package kurtome.dote.scraper

import java.io.StringWriter
import java.net.URL

import org.htmlcleaner._
import scala.xml._

object ScraperMain {

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

  def main(args: Array[String]): Unit = {
    val url = "https://itunes.apple.com/us/genre/podcasts-arts/id1301?mt=2"

    val result = crawlCategory(url)
    result.crawledCategoryLinks.foreach(println(_))
    result.discoveredPodcastLinks.foreach(println(_))
    println(
      s"Found ${result.crawledCategoryLinks.size} category pages and ${result.discoveredPodcastLinks.size} podcasts.")
  }

  private def crawlCategory(url: String): CategoryLinks = {
    val root = CategoryLinks(rootCategory = url, newCategoryLinks = Set(url))
    var current = root
    while (current.newCategoryLinks.size > 0) {
      current = updateLinks(current)
      println(s"Found ${current.newCategoryLinks.size} new category URLs")
      Thread.sleep(500)
    }
    current
  }

  private def updateLinks(existingLinks: CategoryLinks): CategoryLinks = {
    val categoriesAndPodcasts = existingLinks.newCategoryLinks.foldRight(
      (existingLinks.crawledCategoryLinks, existingLinks.discoveredPodcastLinks))(
      (url, categoriesAndPodcasts) => {
        Thread.sleep(10)
        val pageCategoryLinks =
          fetchAllLinksOnPage(url).filter(_.contains(existingLinks.rootCategory))
        val pagePodcastLinks =
          fetchAllLinksOnPage(url).filter(_.startsWith(podcastUrlPrefix))
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
