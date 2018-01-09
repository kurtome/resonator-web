package kurtome.dote.server.controllers

import java.time.Duration
import javax.inject._

import kurtome.dote.server.services.{LoginCodeService, PersonService}
import play.api.mvc._
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginRedirectController @Inject()(
    cc: ControllerComponents,
    personService: PersonService,
    loginCodeService: LoginCodeService)(implicit executionContext: ExecutionContext)
    extends AbstractController(cc)
    with LogSupport {

  private val cookieDuration = Duration.ofDays(120).getSeconds.toInt

  def login(email: String, code: String) = Action async { implicit request: Request[AnyContent] =>
    personService.readByEmail(email) flatMap {
      case Some(person) => {
        loginCodeService.attemptLogin(person.username, person.email, code) map {
          case Some(token) => {
            Redirect("/", FOUND).withCookies(cookie(request, token))
          }
          case None => {
            Redirect("/", FOUND)
          }
        }
      }
      case None => {
        Future(Redirect("/", FOUND))
      }
    }
  }

  private def cookie(request: Request[AnyContent], token: String): Cookie = {
    Cookie("REMEMBER_ME",
           token,
           maxAge = Some(cookieDuration),
           secure = request.secure,
           httpOnly = true,
           domain = Some(request.domain))
  }

}
