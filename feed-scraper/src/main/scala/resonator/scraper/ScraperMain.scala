package resonator.scraper

import java.io.StringWriter
import java.net.URL

import resonator.proto.api.action.add_podcast.AddPodcastRequest.Extras
import resonator.proto.api.action.add_podcast._
import org.htmlcleaner._

import scala.util.Try
import scala.xml._

object ScraperMain {

  private val topCount = 20
  private val popularCount = 10

  private val podcastUrlPrefix: String = "https://itunes.apple.com/us/podcast/"

  private val categoryRoots: Seq[String] = Seq(
    "https://itunes.apple.com/us/genre/podcasts-arts/id1301?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-business/id1321?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-comedy/id1303?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-education/id1304?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-games-hobbies/id1323?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-government-organizations/id1325?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-health/id1307?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-kids-family/id1305?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-music/id1310?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-news-politics/id1311?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-religion-spirituality/id1314?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-science-medicine/id1315?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-society-culture/id1324?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-sports-recreation/id1316?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-tv-film/id1309?mt=2",
    "https://itunes.apple.com/us/genre/podcasts-technology/id1318?mt=2"
  )

  private case class CategoryLinks(rootCategory: String,
                                   newCategoryLinks: Set[String],
                                   crawledCategoryLinks: Set[String] = Set(),
                                   discoveredPodcastLinks: Set[String] = Set(),
                                   popularPodcastLinks: Set[String] = Set())

  case class ScraperConfig(depth: Int = 0,
                           prod: Boolean = false,
                           ingestAllPodcasts: Boolean = false) {
    def topPodcastsOnly = !ingestAllPodcasts && (depth == 0)
  }

  // Documentation at https://github.com/scopt/scopt
  private val argParser = new scopt.OptionParser[ScraperConfig]("scraper") {
    head("feed scraper")

    opt[Int]("depth")
      .action((x, c) => {
        c.copy(depth = x)
      })
      .text("recursion depth to crawl to, defaults to 0 for top pages only")

    opt[Unit]("ingestAllPodcasts")
      .action((_, c) => {
        c.copy(ingestAllPodcasts = true)
      })
      .text(
        s"ingest every podcast link found, instead of just the top $topCount. this is automatically on if --deepCrawl is enabled")

    opt[Unit]("prod")
      .action((_, c) => {
        c.copy(prod = true)
      })
      .text("add discovered podcasts tot he prod server instead of local")

    help("help").text("prints this usage text")
  }

  def main(args: Array[String]): Unit = {
    argParser.parse(args, ScraperConfig()) match {
      case Some(config) => crawl(config)
      case None => println("")
    }
  }

  private def crawl(implicit config: ScraperConfig): Unit = {
    val results = categoryRoots.map(crawlCategory)

    val categoryLinksCount = results.map(_.crawledCategoryLinks.size).sum
    val podcastLinksCount = results.map(_.discoveredPodcastLinks.size).sum
    println(s"Found $categoryLinksCount category pages and $podcastLinksCount podcasts.")

    val podcastLinks = results.flatMap(_.discoveredPodcastLinks)
    val popularLinks: Set[String] = Set(results.flatMap(_.popularPodcastLinks): _*)

    val totalCount = podcastLinks.size

    podcastLinks.zipWithIndex foreach {
      case (podcastUrl: String, i: Int) =>
        println(s"\t[$i of $totalCount] Adding $podcastUrl")
        Try {
          val popular = popularLinks.contains(podcastUrl)
          AddPodcastServer.addPodcast(
            AddPodcastRequest(podcastUrl,
                              ingestLater = true,
                              extras = Some(Extras(popular = popular))))
        } recover {
          case t =>
            t.printStackTrace
            println(s"\t\tFailed adding $podcastUrl: ${t.getMessage}")
        }
    }

    println("done")
  }

  private def crawlCategory(url: String)(implicit config: ScraperConfig): CategoryLinks = {
    val root = CategoryLinks(rootCategory = url, newCategoryLinks = Set(url))
    var current = root
    var depth = 0
    do {
      current = updateLinks(current)
      println(s"Found ${current.newCategoryLinks.size} new category URLs")
      depth += 1
      if (depth <= config.depth) {
        Thread.sleep(500)
      }
    } while (current.newCategoryLinks.nonEmpty && depth <= config.depth)

    current
  }

  private def updateLinks(existingLinks: CategoryLinks)(
      implicit config: ScraperConfig): CategoryLinks = {
    val categoriesPodcastsPopular = existingLinks.newCategoryLinks.foldRight(
      (existingLinks.crawledCategoryLinks,
       existingLinks.discoveredPodcastLinks,
       existingLinks.popularPodcastLinks))((url, categoriesPodcastsPopular) => {
      Thread.sleep(10)
      val pageCategoryLinks =
        fetchAllLinksOnPage(url).filter(_.contains(existingLinks.rootCategory))
      val allPagePodcastLinks =
        fetchAllLinksOnPage(url).filter(_.startsWith(podcastUrlPrefix))
      val pagePodcastLinks =
        if (config.topPodcastsOnly) {
          allPagePodcastLinks.take(topCount)
        } else {
          allPagePodcastLinks
        }
      val popularPodcastLinks = allPagePodcastLinks.take(popularCount)
      println(
        s"\tCrawled $url, found ${pageCategoryLinks.size} category pages and ${pagePodcastLinks.size} podcasts (${popularPodcastLinks.size} popular).")
      (categoriesPodcastsPopular._1 ++ pageCategoryLinks,
       categoriesPodcastsPopular._2 ++ pagePodcastLinks,
       categoriesPodcastsPopular._3 ++ popularPodcastLinks)
    })

    existingLinks.copy(
      newCategoryLinks = categoriesPodcastsPopular._1 -- existingLinks.crawledCategoryLinks,
      crawledCategoryLinks = categoriesPodcastsPopular._1 ++ existingLinks.crawledCategoryLinks,
      discoveredPodcastLinks = existingLinks.discoveredPodcastLinks ++ categoriesPodcastsPopular._2,
      popularPodcastLinks = existingLinks.popularPodcastLinks ++ categoriesPodcastsPopular._3
    )
  }

  private def fetchAllLinksOnPage(url: String): Seq[String] = {
    val pageXmlStr = fetchHtmlAsCleanXml(url)
    val pageXml = XML.loadString(pageXmlStr)
    val podcastAnchors = pageXml \\ "a"
    podcastAnchors.map(_ \@ "href")
  }

  private def fetchHtmlAsCleanXml(url: String): String = {
    val cleaner = new HtmlCleaner
    val props = cleaner.getProperties
    val rootNode: TagNode = cleaner.clean(new URL(url))
    val serializer = new SimpleXmlSerializer(new CleanerProperties())
    val writer = new StringWriter()
    rootNode.serialize(serializer, writer)
    writer.toString
  }

}
