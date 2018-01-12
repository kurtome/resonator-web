package kurtome.dote.server.controllers

import java.time.Duration
import javax.inject._

import kurtome.dote.server.services._
import play.api.mvc._
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LogoutRedirectController @Inject()(cc: ControllerComponents)(
    implicit executionContext: ExecutionContext)
    extends AbstractController(cc)
    with LogSupport {

  def logout() = Action { implicit request: Request[AnyContent] =>
    Redirect("/", FOUND).withCookies(logoutRememberMeCookie(request))
  }

  private def logoutRememberMeCookie(request: Request[AnyContent]): Cookie = {
    Cookie("REMEMBER_ME",
           "",
           maxAge = Some(-1), // immediately expire
           secure = request.secure,
           httpOnly = true,
           domain = Some(request.domain))
  }

}
