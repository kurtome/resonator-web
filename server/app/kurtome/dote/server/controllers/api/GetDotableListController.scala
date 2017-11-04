package kurtome.dote.server.controllers.api

import javax.inject._

import dote.proto.api.action.get_dotable_list._
import kurtome.dote.server.db.PodcastDbService
import kurtome.dote.slick.db.DotableKinds
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetDotableListController @Inject()(
    cc: ControllerComponents,
    podcastDbService: PodcastDbService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetDotableListRequest, GetDotableListResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    GetDotableListRequest.parseFrom(bytes)

  override def action(request: GetDotableListRequest): Future[GetDotableListResponse] = {
    podcastDbService
      .readLimited(DotableKinds.Podcast, request.maxResults)
      .map(GetDotableListResponse(_))
  }

}
