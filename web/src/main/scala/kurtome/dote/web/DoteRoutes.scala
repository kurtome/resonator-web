package kurtome.dote.web

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router._
import kurtome.dote.web.components.views.AddPodcastView
import kurtome.dote.web.views.HelloView
import org.scalajs.dom

object DoteRoutes {

  sealed trait DotePage
  case object Home extends DotePage
  case object PageNotFound extends DotePage
  case object Add extends DotePage
  case object Lists extends DotePage

  private val routerConfig: RouterConfig[DotePage] =
    RouterConfigDsl[DotePage].buildConfig { dsl =>
      import dsl._
      (emptyRule
        | staticRedirect("") ~> redirectToPage(Home)(Redirect.Replace)
        | staticRedirect("#") ~> redirectToPage(Home)(Redirect.Replace)
        | staticRedirect("#/") ~> redirectToPage(Home)(Redirect.Replace)
        | staticRoute("#/home", Home) ~> render(AddPodcastView())
        | staticRoute("#/not-found", PageNotFound) ~> render(HelloView.component("who am iii??")))
        .notFound(redirectToPage(PageNotFound)(Redirect.Replace))
        // Verify the Home route is used
        .verify(Home)
    }

  private val baseUrl: BaseUrl =
    BaseUrl(dom.window.location.href.takeWhile(_ != '#'))

  val router = Router(baseUrl, routerConfig)
}
