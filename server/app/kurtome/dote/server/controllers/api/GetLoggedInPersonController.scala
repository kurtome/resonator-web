package kurtome.dote.server.controllers.api

import java.time.Duration
import javax.inject._

import dote.proto.api.action.get_logged_in_person._
import dote.proto.api.common.ResponseStatus
import kurtome.dote.server.controllers.mappers.PersonMapper
import kurtome.dote.server.services._
import play.api.mvc._

import scala.concurrent._

@Singleton
class GetLoggedInPersonController @Inject()(
    cc: ControllerComponents,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetLoggedInPersonRequest, GetLoggedInPersonResponse](cc) {

  private val cookieDuration = Duration.ofDays(120).getSeconds.toInt

  override def parseRequest(bytes: Array[Byte]) = GetLoggedInPersonRequest.parseFrom(bytes)

  override def action(request: Request[GetLoggedInPersonRequest]) = {
    Future(request.cookies.get("REMEMBER_ME")) flatMap {
      case Some(cookie) =>
        authTokenService.readPersonForCookieToken(cookie.value) map { person =>
          GetLoggedInPersonResponse(person = person.map(PersonMapper),
                                    status = Some(
                                      ResponseStatus(success = person.isDefined,
                                                     errorMessage =
                                                       if (person.isDefined) ""
                                                       else "Not logged in.")))
        }
      case None => Future(GetLoggedInPersonResponse())
    }
  }

}
