package kurtome.dote.web

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router._
import kurtome.dote.shared.util.observer.SimpleObservable
import kurtome.dote.web.components.views._
import kurtome.dote.web.components.widgets.ContentFrame
import kurtome.dote.web.views.HelloView
import org.scalajs.dom
import wvlet.log.LogSupport

object DoteRoutes extends LogSupport {

  /////////////////////////////////////////////////////////////////////////////////////////////////

  sealed trait DoteRoute

  case object HomeRoute extends DoteRoute

  case object SearchRoute extends DoteRoute

  case class DetailsRoute(id: String, slug: String) extends DoteRoute

  case object PageNotFoundRoute extends DoteRoute

  case object AddRoute extends DoteRoute

  case object LoginRoute extends DoteRoute

  case object ListsRoute extends DoteRoute

  case object ThemeRoute extends DoteRoute

  case class ProfileRoute(username: String) extends DoteRoute

  case class TagRoute(kind: String, key: String) extends DoteRoute

  /////////////////////////////////////////////////////////////////////////////////////////////////

  type DoteRouterCtl = RouterCtl[DoteRoute]

  private val routerConfig: RouterConfig[DoteRoute] =
    RouterConfigDsl[DoteRoute].buildConfig { dsl =>
      import dsl._

      // Slug must start and end with a alpha-numeric
      val slug = string("(?:[a-z0-9][-a-z0-9]+[a-z0-9])|[a-z0-9]")
      val slug2 = string("([a-z0-9][-a-z0-9]+[a-z0-9])|[a-z0-9]")

      val id = string("[a-zA-Z0-9]+")

      val code = string("[A-Z0-9]+")
      val email = string(".+")

      (emptyRule

        | staticRedirect("") ~> redirectToPage(HomeRoute)(Redirect.Replace)

        | staticRedirect("/") ~> redirectToPage(HomeRoute)(Redirect.Replace)

        | staticRoute("/home", HomeRoute) ~> renderR(_ => HomeView()())

        | staticRoute("/login", LoginRoute) ~> renderR(_ => LoginView()())

        | staticRoute("/search", SearchRoute) ~> renderR(_ => SearchView()())

        | staticRoute("/add", AddRoute) ~> renderR(_ => AddPodcastView()())

        | staticRoute("/theme", ThemeRoute) ~> renderR(_ => ThemeView()())

        | dynamicRouteCT("/tag" ~ ("/" ~ slug ~)("/" ~ slug)
          .caseClassDebug[TagRoute]) ~> dynRenderR(
          (page: TagRoute, routerCtl) => FeedView(page.kind, page.key)())

        | dynamicRouteCT("/details" ~ ("/" ~ id ~ "/" ~ slug)
          .caseClass[DetailsRoute]) ~> dynRenderR(
          (page: DetailsRoute, routerCtl) => DotableDetailView(page)())

        | dynamicRouteCT("/profile" ~ ("/" ~ slug)
          .caseClass[ProfileRoute]) ~> dynRenderR(
          (page: ProfileRoute, routerCtl) => ProfileView(page)())

        | staticRoute("/not-found", PageNotFoundRoute) ~> render(
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
        .onPostRender((prev, cur) =>
          Callback {
            currentRoute = cur
            routeObservable.notifyObservers(cur)
        })
    }

  private val baseUrl: BaseUrl =
    BaseUrl(dom.window.location.protocol + "//" + dom.window.location.host)

  val routeObservable = new SimpleObservable[DoteRoute]

  val doteRouter = Router(baseUrl, routerConfig)

  var doteRouterCtl: DoteRouterCtl = null

  private var currentRoute: DoteRoute = HomeRoute
}
