package kurtome.dote.server.ingestion

import com.google.inject._

import scala.concurrent.duration._
import kurtome.dote.proto.api.action.add_podcast.AddPodcastRequest.Extras
import kurtome.dote.shared.util.result.FailedData
import kurtome.dote.shared.util.result.ProduceAction
import kurtome.dote.shared.util.result.SuccessData
import kurtome.dote.shared.util.result.UnknownErrorStatus
import play.api.libs.ws.WSClient
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class PodcastFeedFetcher @Inject()(ws: WSClient, parser: PodcastFeedParser)(
    implicit ec: ExecutionContext)
    extends LogSupport { self =>

  def fetch(itunesUrl: String,
            feedUrl: String,
            previousEtag: Option[String],
            extras: Extras): Future[ProduceAction[Seq[RssFetchedPodcast]]] = {
    val headers: Seq[(String, String)] =
      if (previousEtag.isDefined) {
        Seq("If-None-Match" -> previousEtag.get)
      } else {
        Nil
      }

    debug(s"Fetching $feedUrl")
    ws.url(feedUrl).withHttpHeaders(headers: _*).withRequestTimeout(5.seconds).get() flatMap {
      response =>
        if (response.status == 200) {
          val etag: Option[String] = response.header("ETag")
          // Remove any spurious leading characters, which will break the parsing
          val startXmlIndex = response.body.indexOf('<')
          if (startXmlIndex >= 0) {
            val xmlString = response.body.substring(startXmlIndex)
            val fetchedPodasts =
              parser.parsePodcastRss(itunesUrl, feedUrl, etag, extras, xmlString)
            filterInvalidPodcasts(fetchedPodasts) map { validPodcasts =>
              if (validPodcasts.nonEmpty) {
                SuccessData(validPodcasts)
              } else {
                FailedData(Nil, UnknownErrorStatus)
              }
            }
          } else {
            info(s"Response wasn't valid feed url: $feedUrl")
            Future(FailedData(Nil, UnknownErrorStatus))
          }
        } else if (response.status == 304) {
          debug(s"Feed unchanged (status was 304) for feed url: $feedUrl")
          Future(SuccessData(Nil))
        } else {
          info(s"Response status was ${response.status} for feed url: $feedUrl")
          Future(FailedData(Nil, UnknownErrorStatus))
        }
    }
  }

  private def filterInvalidPodcasts(
      podcasts: Seq[RssFetchedPodcast]): Future[Seq[RssFetchedPodcast]] = {
    Future.sequence(podcasts.map(toValidPodcast)) map { validPodcasts =>
      validPodcasts.filter(_.isDefined).map(_.get)
    }
  }

  private def toValidPodcast(podcast: RssFetchedPodcast): Future[Option[RssFetchedPodcast]] = {
    val hasAudio = podcast.episodes.exists(_.details.audio.isDefined)

    if (hasAudio) {
      val imageUrl = podcast.details.imageUrl
      Try {
        ws.url(imageUrl).withRequestTimeout(5.seconds).head() map { response =>
          if (response.status == 200) {
            Some(podcast)
          } else {
            debug(s"no image at $imageUrl for ${podcast.feedUrl}, ${response}")
            None
          }
        }
      } recover {
        case t =>
          debug(s"Error getting image at url $imageUrl", t)
          Future(None)
      } get
    } else {
      debug(s"no audio for ${podcast.feedUrl}")
      Future(None)
    }
  }
}
