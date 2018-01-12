package kurtome.dote.server.controllers

import java.time.Duration
import javax.inject._

import kurtome.dote.server.services._
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

  private val rememberMeCookieDuration =
    AuthTokenService.tokenDuration.minusDays(1).getSeconds.toInt

  // expire just after page load
  private val loginAttemptCookieDuration = Duration.ofSeconds(10).getSeconds.toInt

  def login(email: String, code: String) = Action async { implicit request: Request[AnyContent] =>
    personService.readByEmail(email) flatMap {
      case Some(person) => {
        loginCodeService.attemptLogin(person.username, person.email, code) map {
          case Some(token) => {
            Redirect("/", FOUND).withCookies(rememberMeCookie(request, token),
                                             loginAttemptedCookie(request))
          }
          case None => {
            Redirect("/", FOUND).withCookies(loginAttemptedCookie(request))
          }
        }
      }
      case None => {
        Future(Redirect("/", FOUND).withCookies(loginAttemptedCookie(request)))
      }
    }
  }

  private def rememberMeCookie(request: Request[AnyContent], token: String): Cookie = {
    Cookie("REMEMBER_ME",
           token,
           maxAge = Some(rememberMeCookieDuration),
           secure = request.secure,
           httpOnly = true,
           domain = Some(request.domain))
  }

  private def loginAttemptedCookie(request: Request[AnyContent]): Cookie = {
    Cookie(
      "LOGIN_REDIRECT",
      "",
      maxAge = Some(loginAttemptCookieDuration),
      secure = request.secure,
      httpOnly = false, // let the client read this
      domain = Some(request.domain)
    )
  }

}
