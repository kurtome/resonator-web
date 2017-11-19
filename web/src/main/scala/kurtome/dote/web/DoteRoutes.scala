package kurtome.dote.web

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router._
import kurtome.dote.web.components.views._
import kurtome.dote.web.views.HelloView
import org.scalajs.dom

object DoteRoutes {

  /////////////////////////////////////////////////////////////////////////////////////////////////

  sealed trait DoteRoute

  case object HomeRoute extends DoteRoute

  case class DotableRoute(id: String, slug: String) extends DoteRoute

  case object PageNotFoundRoute extends DoteRoute

  case object AddRoute extends DoteRoute

  case object ListsRoute extends DoteRoute

  /////////////////////////////////////////////////////////////////////////////////////////////////

  type DoteRouterCtl = RouterCtl[DoteRoute]

  private val routerConfig: RouterConfig[DoteRoute] =
    RouterConfigDsl[DoteRoute].buildConfig { dsl =>
      import dsl._

      // Slug must start and end with a alpha-numeric
      val slug = string("[a-z0-9][-a-z0-9]+[a-z0-9]")

      val id = string("[a-zA-Z0-9]+")

      (emptyRule
        | staticRedirect("") ~> redirectToPage(HomeRoute)(Redirect.Replace)

        | staticRedirect("#") ~> redirectToPage(HomeRoute)(Redirect.Replace)

        | staticRedirect("#/") ~> redirectToPage(HomeRoute)(Redirect.Replace)

        | staticRoute("#/home", HomeRoute) ~> renderR(HomeView(_)())

        | staticRoute("#/add", AddRoute) ~> renderR(AddPodcastView(_)())

        | dynamicRouteCT("#/podcast" ~ ("/" ~ id ~ "/" ~ slug)
          .caseClass[DotableRoute]) ~> dynRenderR(
          (page: DotableRoute, routerCtl) => DotableDetailView(routerCtl, page)())

        | dynamicRouteCT("#/podcast-episode" ~ ("/" ~ id ~ "/" ~ slug)
          .caseClass[DotableRoute]) ~> dynRenderR(
          (page: DotableRoute, routerCtl) => DotableDetailView(routerCtl, page)())

        | staticRoute("#/not-found", PageNotFoundRoute) ~> render(
          HelloView.component("who am iii??")))
        .notFound(redirectToPage(PageNotFoundRoute)(Redirect.Replace))

        // Verify the Home route is used
        .verify(HomeRoute)
    }

  private val baseUrl: BaseUrl =
    BaseUrl(dom.window.location.href.takeWhile(_ != '#'))

  val router = Router(baseUrl, routerConfig)
}
