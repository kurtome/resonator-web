package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.proto.api.action.get_dotable_list._
import kurtome.dote.server.model.MetadataFlag
import kurtome.dote.server.services.DotableService
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.SuccessStatus
import kurtome.dote.slick.db.DotableKinds
import play.api.mvc._

import scala.concurrent._

@Singleton
class GetDotableListController @Inject()(
    cc: ControllerComponents,
    podcastDbService: DotableService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetDotableListRequest, GetDotableListResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    GetDotableListRequest.parseFrom(bytes)

  override def action(request: Request[GetDotableListRequest]) = {
    podcastDbService
      .readPodcastTagList(DotableKinds.Podcast, MetadataFlag.Ids.popular, request.body.maxResults) map {
      list =>
        GetDotableListResponse(Some(StatusMapper.toProto(SuccessStatus)), list.list)
    }
  }

}
