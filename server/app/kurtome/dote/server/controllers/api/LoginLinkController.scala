package kurtome.dote.server.controllers.api

import java.net.URLEncoder
import javax.inject._

import dote.proto.api.action.login_link._
import dote.proto.api.common.ResponseStatus
import dote.proto.api.person.Person
import kurtome.dote.server.controllers.mappers.{PersonMapper, StatusMapper}
import kurtome.dote.server.email.{EmailClient, PendingMessage}
import kurtome.dote.server.services.{LoginCodeService, PersonService}
import kurtome.dote.server.util.{SideEffectResult, SuccessEffect}
import kurtome.dote.slick.db.gen.Tables
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

    personDbService.readByUsernameAndEmail(request.body.username, request.body.email) flatMap {
      existingPerson =>
        if (existingPerson.isEmpty) {
          personDbService
            .createPerson(request.body.username, request.body.email)
            .flatMap { creationResponse =>
              if (creationResponse.effectSuccess) {
                createCodeAndSendLoginEmail(creationResponse.result.get, linkPrefix)
                  .map(_ => toResponse(creationResponse))
              } else {
                Future(toResponse(creationResponse))
              }
            }
        } else {
          createCodeAndSendLoginEmail(existingPerson.get, linkPrefix) map { _ =>
            toResponse(SuccessEffect(existingPerson))
          }
        }
    }
  }

  private def toResponse(result: SideEffectResult[Option[Tables.PersonRow]]): LoginLinkResponse = {
    LoginLinkResponse(result.result.map(PersonMapper), Option(StatusMapper(result)))
  }

  private def createCodeAndSendLoginEmail(person: Tables.PersonRow,
                                          linkPrefix: String): Future[Unit] = {
    loginCodeService.writeNewCode(person) flatMap { code =>
      val emailEncoded = URLEncoder.encode(person.email, "UTF-8")
      val loginLink = s"$linkPrefix/lc/$emailEncoded/$code"
      emailClient
        .send(PendingMessage(
          person,
          "Login to Resonator",
          s"Welcome, your username is ${person.username}, please login with this link: $loginLink"))
        .map(_ => Unit)
    }
  }

  private def clearEmailField(response: LoginLinkResponse): LoginLinkResponse = {
    response.copy(person = Some(response.getPerson.copy(email = "")))
  }
}
