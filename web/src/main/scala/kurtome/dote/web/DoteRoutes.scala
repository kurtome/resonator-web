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

  type QueryParams = Map[String, String]

  sealed trait DoteRoute {
    val queryParams: QueryParams
  }

  case class HomeRoute(queryParams: QueryParams = Map.empty) extends DoteRoute

  case class SearchRoute(queryParams: QueryParams = Map.empty) extends DoteRoute

  case class DetailsRoute(id: String, slug: String, queryParams: QueryParams = Map.empty)
      extends DoteRoute

  case class PageNotFoundRoute(queryParams: QueryParams = Map.empty) extends DoteRoute

  case class AddRoute(queryParams: QueryParams = Map.empty) extends DoteRoute

  case class LoginRoute(queryParams: QueryParams = Map.empty) extends DoteRoute

  case class ThemeRoute(queryParams: QueryParams = Map.empty) extends DoteRoute

  case class ProfileRoute(username: String, queryParams: QueryParams = Map.empty) extends DoteRoute

  case class FollowersRoute(username: String, queryParams: QueryParams = Map.empty)
      extends DoteRoute

  case class AllActivityRoute(queryParams: QueryParams = Map.empty) extends DoteRoute

  case class FollowingActivityRoute(queryParams: QueryParams = Map.empty) extends DoteRoute

  case class ProfileActivityRoute(username: String, queryParams: QueryParams = Map.empty)
      extends DoteRoute

  case class TagRoute(kind: String, key: String, queryParams: QueryParams = Map.empty)
      extends DoteRoute

  case class RadioDefaultRoute(queryParams: QueryParams = Map.empty) extends DoteRoute

  case class RadioRoute(
      station: String, // station includes frequency and band suffix, e.g. "770kHz"
      queryParams: QueryParams = Map.empty)
      extends DoteRoute

  case class RadioStationDetailRoute(callSign: String, queryParams: QueryParams = Map.empty)
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

        | dynamicRouteCT(("" ~ query).caseClass[HomeRoute]) ~> dynRenderR((_, _) => HomeView()())

        | dynamicRouteCT(("/home" ~ query).caseClass[HomeRoute]) ~> dynRenderR((_,
                                                                                _) => HomeView()())

        | dynamicRouteCT(("/" ~ query).caseClass[HomeRoute]) ~> dynRenderR((_, _) => HomeView()())

        | dynamicRouteCT(("/login" ~ query).caseClass[LoginRoute]) ~> dynRenderR(
          (_, _) => LoginView()())

        | dynamicRouteCT(("/search" ~ query)
          .caseClass[SearchRoute]) ~> dynRenderR((page: SearchRoute, _) => SearchView(page)())

        | dynamicRouteCT(("/add" ~ query).caseClass[AddRoute]) ~> dynRenderR(
          (_, _) => AddPodcastView()())

        | dynamicRouteCT(("/theme" ~ query).caseClass[ThemeRoute]) ~> dynRenderR(
          (_, _) => ThemeView()())

        | dynamicRouteCT(("/tag" ~ "/" ~ slug ~ "/" ~ slug ~ query)
          .caseClass[TagRoute]) ~> dynRenderR((page: TagRoute, _) => FeedView(page)())

        | dynamicRouteCT("/details" ~ ("/" ~ id ~ "/" ~ slug ~ query)
          .caseClass[DetailsRoute]) ~> dynRenderR((page: DetailsRoute,
                                                   _) => DotableDetailView(page)())

        | dynamicRouteCT("/profile" ~ ("/" ~ slug ~ query)
          .caseClass[ProfileRoute]) ~> dynRenderR((page: ProfileRoute, _) => ProfileView(page)())

        | dynamicRouteCT("/profile" ~ ("/" ~ slug ~ "/followers" ~ query)
          .caseClass[FollowersRoute]) ~> dynRenderR((page: FollowersRoute, _) => FeedView(page)())

        | dynamicRouteCT(("/activity" ~ query)
          .caseClass[AllActivityRoute]) ~> dynRenderR(
          (page: AllActivityRoute, _) => FeedView(page)())

        | dynamicRouteCT(("/activity/following" ~ query)
          .caseClass[FollowingActivityRoute]) ~> dynRenderR(
          (page: FollowingActivityRoute, _) => FeedView(page)())

        | dynamicRouteCT(("/profile/" ~ slug ~ "/activity" ~ query)
          .caseClass[ProfileActivityRoute]) ~> dynRenderR(
          (page: ProfileActivityRoute, _) => FeedView(page)())

        | dynamicRouteCT(("/tuner" ~ query)
          .caseClass[RadioDefaultRoute]) ~> dynRenderR(
          (page: RadioDefaultRoute, _) => RadioView(page)())

        | dynamicRouteCT(("/radio/station/" ~ id ~ query)
          .caseClass[RadioStationDetailRoute]) ~> dynRenderR(
          (page: RadioStationDetailRoute, _) => RadioStationDetailView(page)())

        | dynamicRouteCT("/tuner" ~ ("/" ~ id ~ query)
          .caseClass[RadioRoute]) ~> dynRenderR((page: RadioRoute, _) => RadioView(page)())

        | dynamicRouteCT(("/not-found" ~ query).caseClass[PageNotFoundRoute]) ~> dynRenderR(
          (_, _) => HelloView.component("who am iii??")))
        .notFound(redirectToPage(PageNotFoundRoute())(Redirect.Replace))

        // Verify the Home route is used
        .verify(HomeRoute())
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

  private var firstPageLoad = true

  private def updateCurrentRoute(newRoute: DoteRoute): Unit = {
    currentRoute = newRoute
    routeObservable.notifyObservers(newRoute)

    if (firstPageLoad) {
      // don't send a pageview for the first route, since that is hangled by the gtag.js script
      firstPageLoad = false
    } else {
      UniversalAnalytics.pageview()
    }
  }

  private def pageTitle(route: DoteRoute): String = {
    route match {
      case ThemeRoute(_) => s"Theme | Resonator"
      case AddRoute(_) => s"Add Podcast | Resonator"
      case LoginRoute(_) => s"Login | Resonator"
      case AllActivityRoute(_) => s"Activity | Resonator"
      case SearchRoute(params) =>
        s"${params.getOrElse(QueryParamKeys.query, "Search")} | Resonator Search"
      case RadioRoute(station, _) => s"$station | Resonator"
      case RadioStationDetailRoute(callSign, _) => s"$callSign | Resonator"
      case FollowersRoute(username, _) => s"$username Followers | Resonator"
      case FollowingActivityRoute(_) => s"Following Activity | Resonator"
      case ProfileRoute(username, _) => s"$username Profile | Resonator"
      case _ => "Resonator"
    }
  }

  private val baseUrl: BaseUrl =
    BaseUrl(dom.window.location.protocol + "//" + dom.window.location.host)

  val routeObservable = new SimpleObservable[DoteRoute]

  val doteRouter = Router(baseUrl, routerConfig)

  var doteRouterCtl: DoteRouterCtl = null

  private var currentRoute: DoteRoute = HomeRoute()
}
