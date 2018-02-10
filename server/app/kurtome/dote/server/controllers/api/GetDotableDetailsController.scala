package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.proto.api.action.get_dotable._
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.util.UrlIds
import kurtome.dote.server.util.UrlIds.IdKinds
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.ErrorStatus
import kurtome.dote.shared.util.result.StatusCodes
import kurtome.dote.shared.util.result.SuccessStatus
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class GetDotableDetailsController @Inject()(
    cc: ControllerComponents,
    podcastDbService: DotableService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetDotableDetailsRequest, GetDotableDetailsResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    GetDotableDetailsRequest.parseFrom(bytes)

  override def action(request: Request[GetDotableDetailsRequest]) = {
    podcastDbService
      .readDotableWithParentAndChildren(UrlIds.decode(IdKinds.Dotable, request.body.id)) map {
      case Some(dotable) =>
        GetDotableDetailsResponse(Some(StatusMapper.toProto(SuccessStatus)), Some(dotable))
      case None =>
        GetDotableDetailsResponse(Some(StatusMapper.toProto(ErrorStatus(StatusCodes.NotFound))),
                                  None)
    }
  }

}
