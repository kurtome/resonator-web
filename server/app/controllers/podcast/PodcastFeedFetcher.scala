package controllers.podcast

import com.google.inject._
import play.api.libs.ws.WSClient

import scala.xml.{Node, XML}
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime

import dote.proto.model.doteentity._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PodcastFeedFetcher @Inject()(ws: WSClient) { self =>

  val dateFormatter = DateTimeFormatter.RFC_1123_DATE_TIME

  def fetch(url: String): Future[DoteEntity] = {
    ws.url(url).get() map { response =>
      // Remove any spurious leading characters, which will break the parsing
      val xmlString = response.body.substring(response.body.indexOf('<'))
      parsePodcastRss(url, xmlString)
    }
  }

  private def parsePodcastRss(url: String, rssXml: String): DoteEntity = {
    val channels = XML.loadString(rssXml) \ "channel"
    val podcasts = channels.map(parsePodcast(url, _))
    // TODO - support multi-channel RSS feeds for more than one podcast in the feed
    podcasts.head
  }

  private def parsePodcast(url: String, podcast: Node): DoteEntity = {
    val title = (podcast \ "title").head.text
    val description = (podcast \ "description").headOption.map(_.text).getOrElse("")

    val imagesWithHref = (podcast \ "image").filter(n => n.attribute("href").isDefined)
    val imageUrl = imagesWithHref \@ "href"

    val author = (podcast \ "author").text

    val episodeNodes = podcast \ "item"
    val episodes = episodeNodes map parseEpisode

    DoteEntity(Option(CommonInfo(title, description, imageUrl, author)),
               DoteEntity.Details.Podcast(PodcastDetails(episodes)))
  }

  private def parseEpisode(episode: Node): DoteEntity = {
    val title = (episode \ "title").text
    val description = (episode \ "description").text
    val author = (episode \ "author").text
    val pubDate = ZonedDateTime.from(dateFormatter.parse((episode \ "pubDate").text))

    DoteEntity(
      Option(CommonInfo(title = title, descriptionHtml = description, createdBy = author)),
      DoteEntity.Details.PodcastEpisode(PodcastEpisodeDetails()))
  }
}
