package kurtome.dote.server.controllers.api

import javax.inject._

import dote.proto.api.action.get_dotable._
import kurtome.dote.server.db.PodcastDbService
import kurtome.dote.server.util.UrlIds
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetDotableDetailsController @Inject()(
    cc: ControllerComponents,
    podcastDbService: PodcastDbService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetDotableDetailsRequest, GetDotableDetailsResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    GetDotableDetailsRequest.parseFrom(bytes)

  override def action(request: GetDotableDetailsRequest): Future[GetDotableDetailsResponse] = {
    podcastDbService
      .readPodcastWithEpisodes(UrlIds.decode(request.id))
      .map(GetDotableDetailsResponse(_))
  }

}
