package controllers.podcast

import com.google.inject._
import play.api.libs.ws.WSClient

import scala.xml.{Node, NodeSeq, XML}
import java.time.format.DateTimeFormatter
import java.time._
import java.util.Locale

import dote.proto.model.dote_entity._
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Try}

@Singleton
class PodcastFeedFetcher @Inject()(ws: WSClient) { self =>

  val pubDateFormatter = DateTimeFormatter.RFC_1123_DATE_TIME

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
    val title: String = podcast \ "title"
    val description: String = podcast \ "description"

    val imagesWithHref = (podcast \ "image").filter(n => n.attribute("href").isDefined)
    val imageUrl = imagesWithHref \@ "href"

    val websiteUrl: String = podcast \ "link"

    val languageCode: String = podcast \ "language"
    val languageDisplay: String = languageCode match {
      case "" => ""
      case _ =>
        Try(new Locale(languageCode).getDisplayLanguage)
          .recover {
            case t => {
              Logger.error(s"Unable to parse language '$languageCode'", t)
              ""
            }
          }
          .getOrElse("")
    }

    val author: String = podcast \ "author"

    val episodeNodes = podcast \ "item"
    val episodes = episodeNodes map parseEpisode reverse

    DoteEntity(
      Option(
        CommonInfo(
          title = title,
          descriptionHtml = description,
          imageUrl = imageUrl,
          createdBy = author,
          publishedEpochSec = episodes.headOption.map(_.common.get.publishedEpochSec).getOrElse(0),
          updatedEpochSec = episodes.lastOption.map(_.common.get.publishedEpochSec).getOrElse(0)
        )),
      DoteEntity.Details.Podcast(
        PodcastDetails(episodes = episodes,
                       websiteUrl = websiteUrl,
                       languageCode = languageCode,
                       languageDisplay = languageDisplay))
    )
  }

  private def parseEpisode(episode: Node): DoteEntity = {
    val title: String = episode \ "title"
    val description: String = episode \ "summary"
    val author: String = episode \ "author"
    val pubDateEpochSec =
      Try(ZonedDateTime.from(pubDateFormatter.parse(episode \ "pubDate")).toEpochSecond).toOption
    val duration = parseDurationAsSeconds(episode \ "duration")
    val episodeNum = Try(Integer.parseInt(episode \ "episode")).toOption

    DoteEntity(
      Option(
        CommonInfo(title = title,
                   descriptionHtml = description,
                   createdBy = author,
                   publishedEpochSec = pubDateEpochSec.getOrElse(0))),
      DoteEntity.Details.PodcastEpisode(
        PodcastEpisodeDetails(
          durationSec = duration.getOrElse(0),
          episodeNumber = episodeNum.getOrElse(0)
        ))
    )
  }

  private def parseDurationAsSeconds(raw: String): Option[Int] = {
    val tryParse = Try(
      //  Check for HH:MM:SS format
      if (raw.contains(':')) {
        // Assume it's either MM:SS or HH:MM:SS
        val parts = raw.split(':')
        if (parts.size == 2) {
          val minutes = Integer.parseInt(parts(0))
          val seconds = Integer.parseInt(parts(1))
          (minutes * 60) + seconds
        } else if (parts.size == 3) {
          val hours = Integer.parseInt(parts(0))
          val minutes = Integer.parseInt(parts(1))
          val seconds = Integer.parseInt(parts(2))
          (hours * 3600) + (minutes * 60) + seconds
        } else {
          throw new IllegalStateException(s"Unable to parse $raw")
        }
      } else {
        // Assume its raw number of seconds
        Integer.parseInt(raw)
      }
    )

    tryParse match {
      case Failure(t) => Logger.error("", t)
      case _ =>
    }

    tryParse.toOption
  }

  /**
    * Automatically convert a node seq into the text content of the *first* element in the seq.
    * This is useful when selecting a node which should be a singleton with text.
    */
  private implicit def nodeseq2text(nodes: NodeSeq): String = {
    nodes.filter(!_.text.isEmpty).headOption.map(_.text).getOrElse("")
  }
}
