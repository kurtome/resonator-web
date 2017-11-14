package kurtome.dote.scraper

import java.io.StringWriter
import java.net.URL

import dote.proto.api.action.add_podcast.{AddPodcastRequest, AddPodcastResponse}
import org.htmlcleaner._

import scala.util.Try
import scala.xml._

object ScraperMain {

  private val topCount = 5

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
                                   discoveredPodcastLinks: Set[String] = Set())

  case class ScraperConfig(deepCrawl: Boolean = false,
                           prod: Boolean = false,
                           ingestAllPodcasts: Boolean = false) {
    def topPodcastsOnly = !ingestAllPodcasts && !deepCrawl
  }

  // Documentation at https://github.com/scopt/scopt
  private val argParser = new scopt.OptionParser[ScraperConfig]("scraper") {
    head("feed scraper")

    opt[Unit]("deepCrawl")
      .action((_, c) => {
        c.copy(deepCrawl = true)
      })
      .text("crawl all category pages instead of just the top of each category")

    opt[Unit]("ingestAllPodcasts")
      .action((_, c) => {
        c.copy(prod = true)
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
    val url = "https://itunes.apple.com/us/genre/podcasts-arts/id1301?mt=2"

    val results = categoryRoots.map(crawlCategory(_))

    val categoryLinksCount = results.map(_.crawledCategoryLinks.size).sum
    val podcastLinksCount = results.map(_.discoveredPodcastLinks.size).sum
    println(s"Found $categoryLinksCount category pages and $podcastLinksCount podcasts.")

    results
      .flatMap(_.discoveredPodcastLinks)
      .foreach(podcastUrl => {
        println(s"\tAdding $podcastUrl")
        Try {
          AddPodcastServer.addPodcast(AddPodcastRequest(podcastUrl))
        } recover {
          case t =>
            t.printStackTrace
            println(s"\t\tFailed adding $url: ${t.getMessage}")
        }
      })
    println("done")
  }

  private def crawlCategory(url: String)(implicit config: ScraperConfig): CategoryLinks = {
    val root = CategoryLinks(rootCategory = url, newCategoryLinks = Set(url))
    var current = root
    do {
      current = updateLinks(current)
      println(s"Found ${current.newCategoryLinks.size} new category URLs")
      if (config.deepCrawl) {
        Thread.sleep(500)
      }
    } while (current.newCategoryLinks.size > 0 && config.deepCrawl)

    current
  }

  private def updateLinks(existingLinks: CategoryLinks)(
      implicit config: ScraperConfig): CategoryLinks = {
    val categoriesAndPodcasts = existingLinks.newCategoryLinks.foldRight(
      (existingLinks.crawledCategoryLinks, existingLinks.discoveredPodcastLinks))(
      (url, categoriesAndPodcasts) => {
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
        println(
          s"\tCrawled $url, found ${pageCategoryLinks.size} category pages and ${pagePodcastLinks.size} podcasts.")
        (categoriesAndPodcasts._1 ++ pageCategoryLinks,
         categoriesAndPodcasts._2 ++ pagePodcastLinks)
      })

    existingLinks.copy(
      newCategoryLinks = categoriesAndPodcasts._1 -- existingLinks.crawledCategoryLinks,
      crawledCategoryLinks = categoriesAndPodcasts._1 ++ existingLinks.crawledCategoryLinks,
      discoveredPodcastLinks = existingLinks.discoveredPodcastLinks ++ categoriesAndPodcasts._2
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
