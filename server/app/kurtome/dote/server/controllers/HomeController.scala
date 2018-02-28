package kurtome.dote.server.controllers

import javax.inject._
import com.trueaccord.scalapb.json.JsonFormat
import kurtome.dote.proto.api.person.Person
import kurtome.dote.server.controllers.mappers.PersonMapper
import kurtome.dote.server.services.AuthTokenService
import kurtome.dote.server.util.UrlIds
import kurtome.dote.slick.db.gen.Tables
import play.api.mvc._
import views.html.helper.CSRF
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, authTokenService: AuthTokenService)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with LogSupport {

  //val siteTitle = s"Pod ${cryingFace}${heartEyes}${unamusedFace}s"
  val siteTitle = s"Resonator"

  /**
    * Serve the templated html page for the app. Contains the root react node and the scripts to
    * run the react app.
    */
  def index() = Action.async { implicit request: Request[AnyContent] =>
    authTokenService.simplifiedRead(request) map { personRow =>
      // These fields will be viewable by the current recipient of the page request, but since this
      // is for the logged in person, it's ok to include their details.
      val personProto = personRow map { p =>
        Person(
          id = UrlIds.encodePerson(p.id),
          username = p.username,
          email = p.email
        )
      }
      val personJson = personProto.map(JsonFormat.toJsonString(_)).getOrElse("")
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
    * Serve the templated .js script for the web-worker
    */
  def workerJs() = Action {
    val libraryScriptUrl = findJsLibraryBundleUrl("web")
    val appScriptUrl = findJsAppBundleUrl("web")
    Ok(
      kurtome.dote.server.views.js.worker(libraryScriptUrl.replace("\\", ""), appScriptUrl)
    )
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

  /**
    * JSONify the logged in person, which will be read by the LoggedInPersonManager in the client
    */
  private def loggedInPersonJson(p: Tables.PersonRow) = {
    // These fields will be viewable by the current recipient of the page request, but since this
    // is for the logged in person, it's ok to include their details.
    val personProto =
      Person(
        id = UrlIds.encodePerson(p.id),
        username = p.username,
        email = p.email
      )
    JsonFormat.toJsonString(personProto)
  }
}
