package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.proto.api.action.set_dote._
import kurtome.dote.server.services._
import kurtome.dote.server.util.UrlIds
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.FailedData
import kurtome.dote.shared.util.result.SuccessData
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
    authTokenService.readLoggedInPersonFromCookie(request) flatMap {
      case SuccessData(Some(person)) =>
        doteService.writeDote(person.id,
                              UrlIds.decode(UrlIds.IdKinds.Dotable, request.body.dotableId),
                              request.body.getDote) map { _ =>
          response(SuccessStatus)
        }
      case FailedData(_, error) => Future(response(error))
    }
  }

  def response(status: ActionStatus): SetDoteResponse = {
    SetDoteResponse(Some(StatusMapper.toProto(status)))
  }

}
