package kurtome.dote.server.controllers.podcast

import com.google.inject._
import dote.proto.api.action.add_podcast.AddPodcastRequest.Extras
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class PodcastFeedFetcher @Inject()(ws: WSClient, parser: PodcastFeedParser)(
    implicit ec: ExecutionContext) { self =>

  def fetch(itunesUrl: String, feedUrl: String, extras: Extras): Future[Seq[RssFetchedPodcast]] = {
    Try {
      ws.url(feedUrl).get() flatMap { response =>
        // Remove any spurious leading characters, which will break the parsing
        val startXmlIndex = response.body.indexOf('<')
        if (startXmlIndex >= 0) {
          val xmlString = response.body.substring(startXmlIndex)
          val fetchedPodasts = parser.parsePodcastRss(itunesUrl, feedUrl, extras, xmlString)
          filterInvalidPodcasts(fetchedPodasts)
        } else {
          Logger.info(s"Response wasn't valid feed url: $feedUrl")
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
    Try {
      ws.url(podcast.details.imageUrl).head() map { response =>
        if (response.status == 200) {
          Some(podcast)
        } else {
          None
        }
      }
    } recover {
      case t =>
        Future(None)
    } get
  }
}
