package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.server.controllers.podcast._
import dote.proto.api.action.add_podcast._
import kurtome.dote.server.db.DotableDbService
import play.api.Configuration
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddPodcastController @Inject()(
    cc: ControllerComponents,
    config: Configuration,
    itunesEntityFetcher: ItunesEntityFetcher,
    dotableDbService: DotableDbService,
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

  override def action(request: AddPodcastRequest): Future[AddPodcastResponse] = {
    val itunesIdStr = request.itunesUrl.split('/').last.substring(2).replaceAll("\\?.*", "")
    val itunesId = itunesIdStr.toLong
    dotableDbService.getPodcastIngestionRowByItunesId(itunesId) flatMap { ingestRowOpt =>
      if (ingestRowOpt.isEmpty) {
        itunesEntityFetcher.fetch(itunesId, "podcast") flatMap { itunesEntity =>
          assert(itunesEntity.resultCount == 1, "must have exactly 1 result")
          val entity = itunesEntity.results.head
          podcastFeedIngester.fetchFeedAndIngestRequest(request,
                                                        itunesId,
                                                        entity.trackViewUrl,
                                                        entity.feedUrl)
        }
      } else {
        podcastFeedIngester.reingestPodcastByItunesId(itunesId)
      }
    }
  }

}
