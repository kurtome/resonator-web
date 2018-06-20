package resonator.server.ingestion

import java.time.LocalDateTime

import javax.inject._
import resonator.proto.api.action.add_podcast.{AddPodcastRequest, AddPodcastResponse}
import resonator.server.search.SearchClient
import resonator.server.services.DotableService
import resonator.shared.util.result.FailedData
import resonator.shared.util.result.ProduceAction
import resonator.shared.util.result.StatusCodes
import resonator.shared.util.result.SuccessData
import resonator.shared.util.result.UnknownErrorStatus
import resonator.slick.db.gen.Tables.PodcastFeedIngestionRow
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class PodcastFeedIngester @Inject()(itunesEntityFetcher: ItunesEntityFetcher,
                                    podcastFetcher: PodcastFeedFetcher,
                                    dotableService: DotableService)(implicit ec: ExecutionContext)
    extends LogSupport {

  def fetchFeedAndIngestRequest(request: AddPodcastRequest,
                                itunesId: Long,
                                itunesUrl: String,
                                feedUrl: String): Future[ProduceAction[Seq[Long]]] = {
    if (request.ingestLater) {
      dotableService.addFeedForLaterIngestion(itunesId, feedUrl) map { _ =>
        debug(s"added for later $feedUrl")
        SuccessData(Nil)
      }
    } else {
      // use a blank etag to force the GET
      fetchFeedAndIngest(request.getExtras, itunesId, feedUrl, None, itunesUrl)
    }
  }

  def reingestPodcastByItunesId(itunesId: Long): Future[ProduceAction[Seq[Long]]] = {
    Try(
      dotableService.getPodcastIngestionRowByItunesId(itunesId) flatMap { ingestionRowOpt =>
        val ingestionRow = ingestionRowOpt.get
        if (ingestionRow.podcastDotableId.isEmpty) {
          itunesEntityFetcher.fetch(itunesId, "podcast") flatMap { itunesEntity =>
            assert(itunesEntity.resultCount == 1, "must have exactly 1 result")
            val entity = itunesEntity.results.head
            fetchFeedAndIngest(AddPodcastRequest.Extras.defaultInstance,
                               itunesId,
                               entity.feedUrl,
                               Some(ingestionRow),
                               entity.trackViewUrl)
          }
        } else {
          val podcastId = ingestionRow.podcastDotableId.get
          dotableService.readDotableShallow(podcastId) flatMap { dotableOpt =>
            val itunesUrl = dotableOpt.get.getDetails.getPodcast.getExternalUrls.itunes
            fetchFeedAndIngest(AddPodcastRequest.Extras.defaultInstance,
                               itunesId,
                               ingestionRow.feedRssUrl,
                               Some(ingestionRow),
                               itunesUrl)
          }
        }
      }
    ) recover {
      case t: Throwable =>
        error("Reingestion failed.", t)
        Future(FailedData(Nil, UnknownErrorStatus))
    } get
  }

  private def fetchFeedAndIngest(extras: AddPodcastRequest.Extras,
                                 itunesId: Long,
                                 feedUrl: String,
                                 ingestionRow: Option[PodcastFeedIngestionRow],
                                 itunesUrl: String): Future[ProduceAction[Seq[Long]]] = {
    podcastFetcher.fetch(itunesUrl, feedUrl, ingestionRow, extras) flatMap { rssPodcastsResult =>
      if (rssPodcastsResult.isSuccess) {
        Future
          .sequence(rssPodcastsResult.data.map(ingestToDatabase(extras, itunesId, _)))
          .map(SuccessData(_))
      } else {
        if (rssPodcastsResult.status.code == StatusCodes.Unchanged) {
          // the feed wasn't changed since last time it was checked, either from the etag or the
          // contents of the feed being checked against the previously ingested feed
          val waitMinutes: Long = ingestionRow.map(_.reingestWaitMinutes).getOrElse(60L)
          dotableService.updateNextIngestionTimeByItunesId(
            itunesId,
            LocalDateTime.now().plusMinutes(waitMinutes))
          // this should be interpreted as successfully processed
          Future(SuccessData(Nil))
        } else {
          Future(FailedData(Nil, UnknownErrorStatus))
        }
      }
    }
  }

  private def ingestToDatabase(extras: AddPodcastRequest.Extras,
                               itunesId: Long,
                               podcast: RssFetchedPodcast): Future[Long] = {
    val id = dotableService.ingestPodcast(itunesId, podcast)
    if (extras.popular) {
      id.map(dotableService.setPopularTag)
    }
    id
  }

}
