package controllers

import javax.inject._

import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    import views.html.helper.CSRF
    Ok(views.html.main(CSRF.getToken, "Podcasts", jsBundleUrl("web")))
  }

  /**
    * Route for ensuring random URLs go to the main client side app (which has not base route, uses # fragment routing)
    */
  def redirectToRoot(route: String) = Action { implicit request: Request[AnyContent] =>
    Redirect("/")
  }

  def jsBundleUrl(projectName: String): Option[String] = {
    val name = projectName.toLowerCase
    Seq(s"$name-opt-bundle.js", s"$name-fastopt-bundle.js")
      .find(name => getClass.getResource(s"/public/$name") != null)
      .map(routes.Assets.versioned(_).url)
  }
}
