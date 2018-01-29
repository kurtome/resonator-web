package kurtome.dote.server.controllers

import javax.inject._

import com.trueaccord.scalapb.json.JsonFormat
import kurtome.dote.server.controllers.mappers.PersonMapper
import kurtome.dote.server.services.AuthTokenService
import play.api.mvc._
import views.html.helper.CSRF

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, authTokenService: AuthTokenService)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  //val siteTitle = s"Pod ${cryingFace}${heartEyes}${unamusedFace}s"
  val siteTitle = s"Resonator"

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action.async { implicit request: Request[AnyContent] =>
    authTokenService.simplifiedRead(request) map { person =>
      val personJson = person.map(PersonMapper).map(JsonFormat.toJsonString(_)).getOrElse("")
      Ok(
        kurtome.dote.server.views.html
          .main(CSRF.getToken,
                personJson,
                siteTitle,
                findJsLibraryBundleUrl("web"),
                findJsAppBundleUrl("web")))
    }
  }

  /**
    * Route for ensuring random URLs go to the main client side app (which has no base route, uses
    * # fragment routing)
    */
  def redirectToRoot(route: String) = Action { implicit request: Request[AnyContent] =>
    Redirect("/")
  }

  /**
    * https://scalacenter.github.io/scalajs-bundler/reference.html#bundling-mode-library-only
    */
  private def findJsLibraryBundleUrl(projectName: String): String = {
    val name = projectName.toLowerCase
    Seq(s"$name-opt-library.js", s"$name-fastopt-library.js")
      .find(name => getClass.getResource(s"/public/$name") != null)
      .map(controllers.routes.Assets.versioned(_).url)
      .get
  }

  /**
    * https://scalacenter.github.io/scalajs-bundler/reference.html#bundling-mode-library-only
    */
  private def findJsAppBundleUrl(projectName: String): String = {
    val name = projectName.toLowerCase
    Seq(s"$name-opt.js", s"$name-fastopt.js")
      .find(name => getClass.getResource(s"/public/$name") != null)
      .map(controllers.routes.Assets.versioned(_).url)
      .get
  }
}
