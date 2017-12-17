package kurtome.dote.server.ingestion

import com.google.inject._
import dote.proto.api.action.add_podcast.AddPodcastRequest.Extras
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
            extras: Extras): Future[Seq[RssFetchedPodcast]] = {
    Try {
      val headers: Seq[(String, String)] =
        if (previousEtag.isDefined) {
          Seq("If-None-Match" -> previousEtag.get)
        } else {
          Nil
        }

      ws.url(feedUrl).withHttpHeaders(headers: _*).get() flatMap { response =>
        if (response.status == 200) {
          val etag: Option[String] = response.header("ETag")
          // Remove any spurious leading characters, which will break the parsing
          val startXmlIndex = response.body.indexOf('<')
          if (startXmlIndex >= 0) {
            val xmlString = response.body.substring(startXmlIndex)
            val fetchedPodasts =
              parser.parsePodcastRss(itunesUrl, feedUrl, etag, extras, xmlString)
            filterInvalidPodcasts(fetchedPodasts)
          } else {
            info(s"Response wasn't valid feed url: $feedUrl")
            Future(Seq())
          }
        } else {
          info(s"Response status was ${response.status} for feed url: $feedUrl")
          Future(Seq())
        }
      }
    } recover {
      case t: Throwable =>
        error(s"Failed fetching and parsing '$feedUrl'", t)
        Future(Seq())
    } get
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
      Try {
        ws.url(podcast.details.imageUrl).head() map { response =>
          if (response.status == 200) {
            Some(podcast)
          } else {
            None
          }
        }
      }.getOrElse(Future(None))
    } else {
      Future(None)
    }
  }
}
