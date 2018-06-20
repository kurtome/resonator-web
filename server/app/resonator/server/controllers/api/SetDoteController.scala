package resonator.server.controllers.api

import javax.inject._

import resonator.proto.api.action.set_dote._
import resonator.server.services._
import resonator.server.util.UrlIds
import resonator.shared.mapper.StatusMapper
import resonator.shared.util.result.FailedData
import resonator.shared.util.result.SuccessData
import resonator.shared.util.result.UnknownErrorStatus
import resonator.shared.util.result.{ActionStatus, SuccessStatus}
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
                              request.body.getDote,
                              request.body.review.map(_.body)) map { status =>
          response(status)
        }
      case FailedData(_, error) => Future(response(error))
      case _ => Future(response(UnknownErrorStatus))
    }
  }

  def response(status: ActionStatus): SetDoteResponse = {
    SetDoteResponse(Some(StatusMapper.toProto(status)))
  }

}
