package kurtome.dote.server.controllers.api

import java.net.URLEncoder
import java.time.{Duration, LocalDateTime}
import javax.inject._

import kurtome.dote.proto.api.action.login_link._
import kurtome.dote.server.controllers.mappers.PersonMapper
import kurtome.dote.server.email.{EmailClient, PendingMessage}
import kurtome.dote.server.services.{LoginCodeService, PersonService}
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.validation.LoginFieldsValidation
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.shared.util.result._
import play.api.mvc._

import scala.concurrent._

/**
  * This routes creates accounts and sends login links to the account's email, it does not log the
  * person in to the site.
  */
@Singleton
class LoginLinkController @Inject()(cc: ControllerComponents,
                                    loginCodeService: LoginCodeService,
                                    personDbService: PersonService,
                                    emailClient: EmailClient)(implicit ec: ExecutionContext)
    extends ProtobufController[LoginLinkRequest, LoginLinkResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) = LoginLinkRequest.parseFrom(bytes)

  override def action(request: Request[LoginLinkRequest]) = {
    val linkPrefix = (if (request.secure) "https://" else "http://") + request.host

    // TODO: move this into the service class
    val usernameStatus = LoginFieldsValidation.username.firstError(request.body.username)
    val emailStatus = LoginFieldsValidation.email.firstError(request.body.email)
    if (!usernameStatus.isSuccess) {
      Future(toResponse(FailedData(None, usernameStatus)))
    } else if (!emailStatus.isSuccess) {
      Future(toResponse(FailedData(None, emailStatus)))
    } else {
      personDbService.readByUsernameAndEmail(request.body.username, request.body.email) flatMap {
        existingPerson =>
          if (existingPerson.isEmpty) {
            personDbService
              .createPerson(request.body.username, request.body.email)
              .flatMap { creationResponse =>
                if (creationResponse.isSuccess) {
                  loginCodeService
                    .createCodeAndSendLoginEmail(creationResponse.data.get.email, linkPrefix)
                    .map(_ => toResponse(creationResponse))
                } else if (creationResponse.status == ErrorStatus(ErrorCauses.EmailAddress,
                                                                  StatusCodes.NotUnique)) {
                  // Email address is in use, already, send a new link to that address
                  loginCodeService
                    .createCodeAndSendLoginEmail(request.body.email, linkPrefix)
                    .map(_ => toResponse(creationResponse))
                } else {
                  Future(toResponse(creationResponse))
                }
              }
          } else {
            loginCodeService.createCodeAndSendLoginEmail(existingPerson.get.email, linkPrefix) map {
              _ =>
                toResponse(SuccessData(existingPerson))
            }
          }
      }
    }
  }

  private def toResponse(result: ProduceAction[Option[Tables.PersonRow]]): LoginLinkResponse = {
    LoginLinkResponse(Some(StatusMapper.toProto(result.status)), result.data.map(PersonMapper))
  }

}
