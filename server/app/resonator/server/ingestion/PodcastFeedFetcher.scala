package resonator.server.ingestion

import java.security.MessageDigest

import com.google.inject._

import scala.concurrent.duration._
import resonator.proto.api.action.add_podcast.AddPodcastRequest.Extras
import resonator.shared.util.result.FailedData
import resonator.shared.util.result.ProduceAction
import resonator.shared.util.result.StatusCodes
import resonator.shared.util.result.SuccessData
import resonator.shared.util.result.UnknownErrorStatus
import resonator.slick.db.gen.Tables.PodcastFeedIngestionRow
import play.api.libs.ws.WSClient
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class PodcastFeedFetcher @Inject()(ws: WSClient, parser: PodcastFeedParser)(
    implicit ec: ExecutionContext)
    extends LogSupport { self =>

  private val sha256 = MessageDigest.getInstance("SHA-256")

  def fetch(itunesUrl: String,
            feedUrl: String,
            ingestionRow: Option[PodcastFeedIngestionRow],
            extras: Extras): Future[ProduceAction[Seq[RssFetchedPodcast]]] = {
    val previousEtag = ingestionRow.flatMap(_.lastFeedEtag).getOrElse("")
    val previousHash = ingestionRow.flatMap(_.lastDataHash).getOrElse(Array[Byte]())

    val headers: Seq[(String, String)] = {
      if (previousEtag.nonEmpty) {
        Seq("If-None-Match" -> previousEtag)
      } else {
        Nil
      }
    }

    debug(s"Fetching $feedUrl")
    ws.url(feedUrl).withHttpHeaders(headers: _*).withRequestTimeout(15.seconds).get() flatMap {
      response =>
        if (response.status == 200) {
          val etag: Option[String] = response.header("ETag")

          val dataHash = sha256.digest(response.bodyAsBytes.toArray)
          if (dataHash sameElements previousHash) {
            debug(s"Feed data hash unchanged for feed url: $feedUrl")
            Future(FailedData(Nil, StatusCodes.Unchanged))
          } else {
            val fetchedPodasts =
              parser.parsePodcastRss(itunesUrl,
                                     feedUrl,
                                     etag,
                                     dataHash,
                                     extras,
                                     response.bodyAsBytes.toArray)
            filterInvalidPodcasts(fetchedPodasts) map { validPodcasts =>
              if (validPodcasts.nonEmpty) {
                SuccessData(validPodcasts)
              } else {
                FailedData(Nil, UnknownErrorStatus)
              }
            }
          }
        } else if (response.status == 304) {
          debug(s"Feed unchanged (status was 304) for feed url: $feedUrl")
          Future(FailedData(Nil, StatusCodes.Unchanged))
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
        ws.url(imageUrl).withRequestTimeout(10.seconds).head() map { response =>
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
