package resonator.server.controllers

import javax.inject._
import com.trueaccord.scalapb.json.JsonFormat
import resonator.proto.api.person.Person
import resonator.server.controllers.mappers.PersonMapper
import resonator.server.services.AuthTokenService
import resonator.server.util.UrlIds
import resonator.slick.db.gen.Tables
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
    *
    * @param route the current route e.g. "profile/person1", the React app will use this once
    *              the client loads the page to render the correct view
    */
  def index(route: String = "") = Action.async { implicit request: Request[AnyContent] =>
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
        resonator.server.views.html
          .main(CSRF.getToken,
                personJson,
                siteTitle,
                findJsLibraryBundleUrl("web"),
                findJsAppBundleUrl("web")))
    }
  }

  /**
    * Serve the templated .js script for the web-worker
    */
  def workerJs() = Action {
    val libraryScriptUrl = findJsLibraryBundleUrl("web")
    val appScriptUrl = findJsAppBundleUrl("web")
    Ok(
      resonator.server.views.js.worker(libraryScriptUrl.replace("\\", ""), appScriptUrl)
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
