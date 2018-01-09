package kurtome.dote.server.controllers.api

import javax.inject._

import dote.proto.api.action.get_dotable_list._
import kurtome.dote.server.db.{DotableDbService, MetadataFlag}
import kurtome.dote.slick.db.{DotableKinds}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetDotableListController @Inject()(
    cc: ControllerComponents,
    podcastDbService: DotableDbService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetDotableListRequest, GetDotableListResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    GetDotableListRequest.parseFrom(bytes)

  override def action(request: Request[GetDotableListRequest]) = {
    podcastDbService
      .readTagList(DotableKinds.Podcast, MetadataFlag.Ids.popular, request.body.maxResults)
      .map(list => GetDotableListResponse(list.map(_.list).getOrElse(Nil)))
  }

}
