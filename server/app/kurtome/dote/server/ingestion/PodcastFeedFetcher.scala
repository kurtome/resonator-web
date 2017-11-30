package kurtome.dote.server.ingestion

import com.google.inject._
import dote.proto.api.action.add_podcast.AddPodcastRequest.Extras
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class PodcastFeedFetcher @Inject()(ws: WSClient, parser: PodcastFeedParser)(
    implicit ec: ExecutionContext) { self =>

  def fetch(itunesUrl: String,
            feedUrl: String,
            previousEtag: String,
            extras: Extras): Future[Seq[RssFetchedPodcast]] = {
    Try {
      val etag = if (previousEtag.nonEmpty) previousEtag else "*"
      ws.url(feedUrl).withHttpHeaders("If-None-Match" -> etag).get() flatMap { response =>
        if (response.status == 200) {
          val etag: String = response.header("ETag").getOrElse("")
          // Remove any spurious leading characters, which will break the parsing
          val startXmlIndex = response.body.indexOf('<')
          if (startXmlIndex >= 0) {
            val xmlString = response.body.substring(startXmlIndex)
            val fetchedPodasts =
              parser.parsePodcastRss(itunesUrl, feedUrl, etag, extras, xmlString)
            filterInvalidPodcasts(fetchedPodasts)
          } else {
            Logger.info(s"Response wasn't valid feed url: $feedUrl")
            Future(Seq())
          }
        } else {
          Logger.info(s"Response status was ${response.status} for feed url: $feedUrl")
          Future(Seq())
        }
      }
    } recover {
      case t: Throwable =>
        Logger.error(s"Failed fetching and parsing '$feedUrl'", t)
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
