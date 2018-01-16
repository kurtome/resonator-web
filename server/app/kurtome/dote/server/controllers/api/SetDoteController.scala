package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.proto.api.action.set_dote._
import kurtome.dote.server.services._
import kurtome.dote.server.util.UrlIds
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.{ActionStatus, SuccessStatus}
import play.api.mvc._

import scala.concurrent._

@Singleton
class SetDoteController @Inject()(
    cc: ControllerComponents,
    doteService: DoteService,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext)
    extends ProtobufController[SetDoteRequest, SetDoteResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    SetDoteRequest.parseFrom(bytes)

  override def action(request: Request[SetDoteRequest]) = {
    authTokenService.readLoggedInPersonFromCookie(request) flatMap { result =>
      if (result.isSuccess) {
        doteService.writeDote(result.data.get.id,
                              UrlIds.decode(UrlIds.IdKinds.Dotable, request.body.dotableId),
                              request.body.getDote) map { _ =>
          response(SuccessStatus)
        }
      } else {
        Future(response(result.status))
      }
    }
  }

  def response(status: ActionStatus): SetDoteResponse = {
    SetDoteResponse(Some(StatusMapper.toProto(status)))
  }

}
