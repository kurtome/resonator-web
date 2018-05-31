package kurtome.dote.web

import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.constants.QueryParamKeys
import kurtome.dote.shared.util.observer.SimpleObservable
import kurtome.dote.web.components.views._
import kurtome.dote.web.components.widgets.ContentFrame
import kurtome.dote.web.utils.UniversalAnalytics
import kurtome.dote.web.views.HelloView
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope
import scala.scalajs.js.annotation.JSName

object DoteRoutes extends LogSupport {

  /////////////////////////////////////////////////////////////////////////////////////////////////

  sealed trait DoteRoute

  case object HomeRoute extends DoteRoute

  case class SearchRoute(queryParams: Map[String, String] = Map.empty) extends DoteRoute

  case class DetailsRoute(id: String, slug: String, queryParams: Map[String, String] = Map())
      extends DoteRoute

  case object PageNotFoundRoute extends DoteRoute

  case object AddRoute extends DoteRoute

  case object LoginRoute extends DoteRoute

  case object ThemeRoute extends DoteRoute

  case class ProfileRoute(username: String) extends DoteRoute

  case class FollowersRoute(username: String) extends DoteRoute

  case class AllActivityRoute(queryParams: Map[String, String] = Map.empty) extends DoteRoute

  case class FollowingActivityRoute(queryParams: Map[String, String] = Map.empty) extends DoteRoute

  case class ProfileActivityRoute(username: String, queryParams: Map[String, String] = Map.empty)
      extends DoteRoute

  case class TagRoute(kind: String, key: String, queryParams: Map[String, String] = Map.empty)
      extends DoteRoute

  case class RadioDefaultRoute(queryParams: Map[String, String] = Map.empty) extends DoteRoute

  case class RadioRoute(
      station: String, // station includes frequency and band suffix, e.g. "770kHz"
      queryParams: Map[String, String] = Map.empty)
      extends DoteRoute

  /////////////////////////////////////////////////////////////////////////////////////////////////

  type DoteRouterCtl = RouterCtl[DoteRoute]

  object AdditionalDsl {
    import StaticDsl._

    private val queryCaptureRegex = "(:?(\\?.*)|())#?|$"

    /**
      * Captures the query portion of the URL to a param map.
      * Note that this is not a strict capture, URLs without a query string will still be accepted,
      * and the parameter map will simply by empty.
      */
    def query: RouteB[Map[String, String]] =
      new RouteB[Map[String, String]](
        queryCaptureRegex,
        1,
        capturedGroups => {
          val capturedQuery = capturedGroups(0).replaceFirst("\\?", "")
          val params = capturedQuery.split("&").filter(_.nonEmpty) map { param =>
            // Note that it is possible for there to be just a key and no value
            val key = param.takeWhile(_ != '=')
            val value = param.drop(key.length + 1)
            key -> js.URIUtils.decodeURIComponent(value)
          }
          Some(params.toMap)
        },
        paramsMap => {
          paramsMap.foldLeft("")((str, param) => {
            if (str.isEmpty) {
              s"?${param._1}=${param._2}"
            } else {
              str + s"&${param._1}=${param._2}"
            }
          })
        }
      )
  }

  private val routerConfig: RouterConfig[DoteRoute] =
    RouterConfigDsl[DoteRoute].buildConfig { dsl =>
      import AdditionalDsl._
      import dsl._

      // Slug must start and end with a alpha-numeric
      val slug = string("(?:[a-z0-9][-a-z0-9_]+[a-z0-9_])|[a-z0-9]")

      val id = string("[a-zA-Z0-9]+")

      val code = string("[A-Z0-9]+")
      val email = string(".+")

      (emptyRule

        | staticRedirect("") ~> redirectToPage(HomeRoute)(Redirect.Replace)

        | staticRedirect("/home") ~> redirectToPage(HomeRoute)(Redirect.Replace)

        | staticRoute("/", HomeRoute) ~> renderR(_ => HomeView()())

        | staticRoute("/login", LoginRoute) ~> renderR(_ => LoginView()())

        | dynamicRouteCT(("/search" ~ query)
          .caseClass[SearchRoute]) ~> dynRenderR((page: SearchRoute,
                                                  routerCtl) => SearchView(page)())

        | staticRoute("/add", AddRoute) ~> renderR(_ => AddPodcastView()())

        | staticRoute("/theme", ThemeRoute) ~> renderR(_ => ThemeView()())

        | dynamicRouteCT(("/tag" ~ "/" ~ slug ~ "/" ~ slug ~ query)
          .caseClass[TagRoute]) ~> dynRenderR((page: TagRoute, routerCtl) => FeedView(page)())

        | dynamicRouteCT("/details" ~ ("/" ~ id ~ "/" ~ slug ~ query)
          .caseClass[DetailsRoute]) ~> dynRenderR((page: DetailsRoute,
                                                   routerCtl) => DotableDetailView(page)())

        | dynamicRouteCT("/profile" ~ ("/" ~ slug)
          .caseClass[ProfileRoute]) ~> dynRenderR((page: ProfileRoute,
                                                   routerCtl) => ProfileView(page)())

        | dynamicRouteCT("/profile" ~ ("/" ~ slug ~ "/followers")
          .caseClass[FollowersRoute]) ~> dynRenderR(
          (page: FollowersRoute, routerCtl) => FeedView(page)())

        | dynamicRouteCT(("/activity" ~ query)
          .caseClass[AllActivityRoute]) ~> dynRenderR(
          (page: AllActivityRoute, routerCtl) => FeedView(page)())

        | dynamicRouteCT(("/activity/following" ~ query)
          .caseClass[FollowingActivityRoute]) ~> dynRenderR(
          (page: FollowingActivityRoute, routerCtl) => FeedView(page)())

        | dynamicRouteCT(("/profile/" ~ slug ~ "/activity" ~ query)
          .caseClass[ProfileActivityRoute]) ~> dynRenderR(
          (page: ProfileActivityRoute, routerCtl) => FeedView(page)())

        | dynamicRouteCT(("/tuner" ~ query)
          .caseClass[RadioDefaultRoute]) ~> dynRenderR(
          (page: RadioDefaultRoute, routerCtl) => RadioView(page)())

        | dynamicRouteCT("/tuner" ~ ("/" ~ id ~ query)
          .caseClass[RadioRoute]) ~> dynRenderR((page: RadioRoute, routerCtl) => RadioView(page)())

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
            updateCurrentRoute(cur)
        })
        .setTitle(pageTitle)
    }

  def replaceCurrentRoute(newRoute: DoteRoute): Unit = {
    val url = doteRouterCtl.urlFor(newRoute).value
    val title = pageTitle(newRoute)
    dom.window.history.replaceState(new js.Object(), title, url)
    dom.document.title = title
    updateCurrentRoute(newRoute)
  }

  private def updateCurrentRoute(newRoute: DoteRoute): Unit = {
    currentRoute = newRoute
    routeObservable.notifyObservers(newRoute)
    UniversalAnalytics.visitor.pageview(dom.window.location.pathname).send()
  }

  private def pageTitle(route: DoteRoute): String = {
    route match {
      case ThemeRoute => s"Theme | Resonator"
      case AddRoute => s"Add Podcast | Resonator"
      case LoginRoute => s"Login | Resonator"
      case AllActivityRoute(_) => s"Activity | Resonator"
      case SearchRoute(params) =>
        s"${params.getOrElse(QueryParamKeys.query, "Search")} | Resonator Search"
      case RadioRoute(station, _) => s"$station | Resonator"
      case FollowersRoute(username) => s"$username Followers | Resonator"
      case FollowingActivityRoute(_) => s"Following Activity | Resonator"
      case ProfileRoute(username) => s"$username Profile | Resonator"
      case _ => "Resonator"
    }
  }

  private val baseUrl: BaseUrl =
    BaseUrl(dom.window.location.protocol + "//" + dom.window.location.host)

  val routeObservable = new SimpleObservable[DoteRoute]

  val doteRouter = Router(baseUrl, routerConfig)

  var doteRouterCtl: DoteRouterCtl = null

  private var currentRoute: DoteRoute = HomeRoute
}
