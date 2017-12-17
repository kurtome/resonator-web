package kurtome.dote.server.ingestion

import javax.inject._

import dote.proto.api.action.add_podcast.{AddPodcastRequest, AddPodcastResponse}
import kurtome.dote.server.db.DotableDbService
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class PodcastFeedIngester @Inject()(
    itunesEntityFetcher: ItunesEntityFetcher,
    podcastFetcher: PodcastFeedFetcher,
    podcastDbService: DotableDbService)(implicit ec: ExecutionContext)
    extends LogSupport {

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
      fetchFeedAndIngest(request.getExtras, itunesId, feedUrl, None, itunesUrl)
    }
  }

  def reingestPodcastByItunesId(itunesId: Long): Future[AddPodcastResponse] = {
    Try(
      podcastDbService.getPodcastIngestionRowByItunesId(itunesId) flatMap { ingestionRowOpt =>
        val ingestionRow = ingestionRowOpt.get
        if (ingestionRow.podcastDotableId.isEmpty) {
          itunesEntityFetcher.fetch(itunesId, "podcast") flatMap { itunesEntity =>
            assert(itunesEntity.resultCount == 1, "must have exactly 1 result")
            val entity = itunesEntity.results.head
            fetchFeedAndIngest(AddPodcastRequest.Extras.defaultInstance,
                               itunesId,
                               entity.feedUrl,
                               ingestionRow.lastFeedEtag,
                               entity.trackViewUrl)
          }
        } else {
          val podcastId = ingestionRow.podcastDotableId.get
          podcastDbService.readDotableShallow(podcastId) flatMap { dotableOpt =>
            val itunesUrl = dotableOpt.get.getDetails.getPodcast.getExternalUrls.itunes
            fetchFeedAndIngest(AddPodcastRequest.Extras.defaultInstance,
                               itunesId,
                               ingestionRow.feedRssUrl,
                               ingestionRow.lastFeedEtag,
                               itunesUrl)
          }
        }
      }
    ) recover {
      case t: Throwable =>
        error("Reingestion failed.", t)
        Future(AddPodcastResponse.defaultInstance)
    } get
  }

  def fetchFeedAndIngest(extras: AddPodcastRequest.Extras,
                         itunesId: Long,
                         feedUrl: String,
                         previousFeedEtag: Option[String],
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
