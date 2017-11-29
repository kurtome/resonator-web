package kurtome.dote.server.controllers.podcast

import javax.inject._

import dote.proto.api.action.add_podcast.{AddPodcastRequest, AddPodcastResponse}
import kurtome.dote.server.db.DotableDbService
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class PodcastFeedIngester @Inject()(
    podcastFetcher: PodcastFeedFetcher,
    podcastDbService: DotableDbService)(implicit ec: ExecutionContext) {

  def fetchFeedAndIngestRequest(request: AddPodcastRequest,
                                itunesId: Long,
                                itunesUrl: String,
                                feedUrl: String): Future[AddPodcastResponse] = {
    if (request.ingestLater) {
      podcastDbService.addFeedForLaterIngestion(itunesId, feedUrl) map { _ =>
        AddPodcastResponse()
      }
    } else {
      // use a blank etag to force the GET
      val previousEtag = ""
      fetchFeedAndIngest(request.getExtras, itunesId, feedUrl, previousEtag, itunesUrl)
    }
  }

  def reingestPodcastByItunesId(itunesId: Long): Future[AddPodcastResponse] = {
    Try(
      podcastDbService.getPodcastIngestionRowByItunesId(itunesId) flatMap { ingestionRowOpt =>
        val ingestionRow = ingestionRowOpt.get
        val podcastId = ingestionRow.podcastDotableId.get
        podcastDbService.readDotableShallow(podcastId) flatMap { dotableOpt =>
          val itunesUrl = dotableOpt.get.getDetails.getPodcast.getExternalUrls.itunes
          fetchFeedAndIngest(AddPodcastRequest.Extras.defaultInstance,
                             itunesId,
                             ingestionRow.feedRssUrl,
                             ingestionRow.lastFeedEtag,
                             itunesUrl) flatMap { _ =>
            podcastDbService.readPodcastWithEpisodes(podcastId)
          } map { podcast =>
            AddPodcastResponse(podcast.map(Seq(_)).getOrElse(Nil))
          }
        }
      }
    ) recover {
      case t: Throwable =>
        Logger.error("Reingestion failed.", t)
        Future(AddPodcastResponse.defaultInstance)
    } get
  }

  def fetchFeedAndIngest(extras: AddPodcastRequest.Extras,
                         itunesId: Long,
                         feedUrl: String,
                         previousFeedEtag: String,
                         itunesUrl: String): Future[AddPodcastResponse] = {
    val eventualPodcast: Future[Seq[RssFetchedPodcast]] =
      podcastFetcher.fetch(itunesUrl, feedUrl, previousFeedEtag, extras)

    eventualPodcast flatMap { rssPodcasts =>
      Future.sequence(rssPodcasts.map(ingestToDatabase(extras, itunesId, _)))
    } flatMap { podcastIds =>
      Future.sequence(podcastIds.map(podcastDbService.readPodcastWithEpisodes))
    } map { podcasts =>
      AddPodcastResponse(podcasts.filter(_.isDefined).map(_.get))
    }

  }

  private def ingestToDatabase(extras: AddPodcastRequest.Extras,
                               itunesId: Long,
                               podcast: RssFetchedPodcast): Future[Long] = {
    val id = podcastDbService.ingestPodcast(itunesId, podcast)
    if (extras.popular) {
      id.map(podcastDbService.setPopularTag)
    }
    id
  }

}
