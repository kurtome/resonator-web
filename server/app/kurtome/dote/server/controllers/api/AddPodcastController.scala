package kurtome.dote.server.controllers.api

import javax.inject._
import kurtome.dote.proto.api.action.add_podcast._
import kurtome.dote.server.ingestion.{ItunesEntityFetcher, PodcastFeedIngester}
import kurtome.dote.server.services.DotableService
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.ProduceAction
import kurtome.dote.shared.util.result.SuccessStatus
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddPodcastController @Inject()(
    cc: ControllerComponents,
    config: Configuration,
    itunesEntityFetcher: ItunesEntityFetcher,
    dotableDbService: DotableService,
    podcastFeedIngester: PodcastFeedIngester)(implicit ec: ExecutionContext)
    extends ProtobufController[AddPodcastRequest, AddPodcastResponse](cc) {

  val allowExtras = config.get[Boolean]("kurtome.dote.add.podcast.extras")

  override def parseRequest(bytes: Array[Byte]): AddPodcastRequest = {
    val req = AddPodcastRequest.parseFrom(bytes)
    if (allowExtras) {
      req
    } else {
      req.update(_.extras := AddPodcastRequest.Extras.defaultInstance)
    }
  }

  override def action(request: Request[AddPodcastRequest]) = {
    val itunesIdStr = request.body.itunesUrl.split('/').last.substring(2).replaceAll("\\?.*", "")
    val itunesId = itunesIdStr.toLong
    dotableDbService.getPodcastIngestionRowByItunesId(itunesId) flatMap {
      case Some(row) => {
        if (request.body.ingestLater) {
          debug("Podcast already exists, skipping ingestion.")
          //Future(AddPodcastResponse().withResponseStatus(StatusMapper.toProto(SuccessStatus)))
          fetchFromItunesAndIngest(request.body, itunesId)
        } else {
          debug("Podcast exists, ingesting anyways.")
          fetchFromItunesAndIngest(request.body, itunesId)
        }
      }
      case None => {
        debug("New podcast, ingesting.")
        fetchFromItunesAndIngest(request.body, itunesId)
      }
    }
  }

  private def readIngestedPodcasts(podcastIds: ProduceAction[Seq[Long]]) = {
    if (podcastIds.isSuccess) {
      Future.sequence(podcastIds.data.map(dotableDbService.readPodcastWithEpisodes)) map {
        podcasts =>
          AddPodcastResponse(podcasts = podcasts.filter(_.isDefined).map(_.get))
      }
    } else {
      Future(AddPodcastResponse.defaultInstance)
    }
  }

  private def fetchFromItunesAndIngest(request: AddPodcastRequest,
                                       itunesId: Long): Future[AddPodcastResponse] = {
    itunesEntityFetcher.fetch(itunesId, "podcast") flatMap { itunesEntity =>
      assert(itunesEntity.resultCount == 1, "must have exactly 1 result")
      debug(s"fetched ${itunesEntity.results.head.trackName} for ingestion")
      val entity = itunesEntity.results.head
      val ingestedPodcasts = podcastFeedIngester.fetchFeedAndIngestRequest(request,
                                                                           itunesId,
                                                                           entity.trackViewUrl,
                                                                           entity.feedUrl)
      ingestedPodcasts.flatMap(readIngestedPodcasts)
    }
  }
}
