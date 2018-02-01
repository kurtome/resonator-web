package kurtome.dote.web

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router._
import kurtome.dote.web.components.views._
import kurtome.dote.web.components.widgets.ContentFrame
import kurtome.dote.web.views.HelloView
import org.scalajs.dom

object DoteRoutes {

  /////////////////////////////////////////////////////////////////////////////////////////////////

  sealed trait DoteRoute

  case object HomeRoute extends DoteRoute

  case object SearchRoute extends DoteRoute

  case class DetailsRoute(id: String, slug: String) extends DoteRoute

  case object PageNotFoundRoute extends DoteRoute

  case object AddRoute extends DoteRoute

  case object LoginRoute extends DoteRoute

  case object ListsRoute extends DoteRoute

  case class ProfileRoute(username: String) extends DoteRoute

  /////////////////////////////////////////////////////////////////////////////////////////////////

  type DoteRouterCtl = RouterCtl[DoteRoute]

  private val routerConfig: RouterConfig[DoteRoute] =
    RouterConfigDsl[DoteRoute].buildConfig { dsl =>
      import dsl._

      // Slug must start and end with a alpha-numeric
      val slug = string("[a-z0-9][-a-z0-9]+[a-z0-9]")

      val id = string("[a-zA-Z0-9]+")

      val code = string("[A-Z0-9]+")
      val email = string(".+")

      (emptyRule
        | staticRedirect("") ~> redirectToPage(HomeRoute)(Redirect.Replace)

        | staticRedirect("#") ~> redirectToPage(HomeRoute)(Redirect.Replace)

        | staticRedirect("#/") ~> redirectToPage(HomeRoute)(Redirect.Replace)

        | staticRoute("#/home", HomeRoute) ~> renderR(_ => HomeView()())

      // the NavBar will ensure the login dialog is shown over the home page
        | staticRoute("#/login", LoginRoute) ~> renderR(_ => HomeView()())

        | staticRoute("#/search", SearchRoute) ~> renderR(_ => SearchView()())

        | staticRoute("#/add", AddRoute) ~> renderR(_ => AddPodcastView()())

        | dynamicRouteCT("#/details" ~ ("/" ~ id ~ "/" ~ slug)
          .caseClass[DetailsRoute]) ~> dynRenderR(
          (page: DetailsRoute, routerCtl) => DotableDetailView(page)())

        | dynamicRouteCT("#/profile" ~ ("/" ~ slug)
          .caseClass[ProfileRoute]) ~> dynRenderR(
          (page: ProfileRoute, routerCtl) => ProfileView(page)())

        | staticRoute("#/not-found", PageNotFoundRoute) ~> render(
          HelloView.component("who am iii??")))
        .notFound(redirectToPage(PageNotFoundRoute)(Redirect.Replace))

        // Verify the Home route is used
        .verify(HomeRoute)
        .renderWith((routerCtl, resolution) => {
          doteRouterCtl = routerCtl
          ContentFrame(resolution.page)(
            resolution.render()
          )
        })
        .setPostRender((prev, cur) =>
          Callback {
            currentRoute = cur
        })
    }

  private val baseUrl: BaseUrl =
    BaseUrl(dom.window.location.href.takeWhile(_ != '#'))

  val doteRouter = Router(baseUrl, routerConfig)

  var doteRouterCtl: DoteRouterCtl = null

  private var currentRoute: DoteRoute = HomeRoute
}
