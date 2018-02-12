package kurtome.dote.web.components.widgets

import kurtome.dote.proto.api.person.Person
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.util.observer.Observer
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.Debounce
import kurtome.dote.web.utils.IsMobile
import kurtome.dote.web.utils.{GlobalLoadingManager, LoggedInPersonManager}
import org.scalajs.dom
import org.scalajs.dom.raw.MutationObserverInit
import wvlet.log.LogSupport

import scala.scalajs.js
import scalacss.internal.mutable.StyleSheet

/**
  * Navigation controls at the bottom of each page.
  */
object NavBar extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val pageLengthHack = style(
      position.fixed,
      width(100 %%),
      bottom(0 px)
    )

    val bottomFixedNavRoot = style(
      position.fixed,
      width(100 %%),
      bottom(0 px)
    )

    val bottomNavRoot = style(
      position.absolute,
      marginTop(SharedStyles.spacingUnit),
      width(100 %%),
      bottom(0 px)
    )
  }

  case class Props(currentRoute: DoteRoute)
  case class State(navValue: String = "home",
                   isCollapsed: Boolean = false,
                   isLoading: Boolean = false,
                   windowHeight: Int = 0,
                   documentHeight: Int = 0,
                   pageYOffset: Int = 0,
                   loggedInPerson: Option[Person] = None)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val loadingObserver: Observer[GlobalLoadingManager.State] =
      (gs: GlobalLoadingManager.State) => {
        bs.modState(_.copy(isLoading = gs.isLoading)).runNow()
        bs.forceUpdate.runNow()
      }

    GlobalLoadingManager.stateObservable.addObserver(loadingObserver)

    val onMount: Callback = Callback {
      dom.window.addEventListener("resize", resizeListener)
      dom.window.addEventListener("scroll", scrollListener)
      routeObservable.addObserver(routeObserver)
      updateIsCollapsed()
    }

    val onUnmount: Callback = Callback {
      recalcCollapseTimerId.foreach(id => dom.window.clearTimeout(id))
      GlobalLoadingManager.stateObservable.removeObserver(loadingObserver)
      dom.window.removeEventListener("resize", resizeListener)
      dom.window.removeEventListener("scroll", scrollListener)
      routeObservable.removeObserver(routeObserver)
    }

    val handleProfileButtonClicked = (p: Props) =>
      if (LoggedInPersonManager.isLoggedIn) {
        doteRouterCtl.set(ProfileRoute(LoggedInPersonManager.person.get.username))
      } else {
        doteRouterCtl.set(LoginRoute)
    }

    def updateIsCollapsed(): Unit = {
      // hide the nav bar if the vertical space is very small.
      // this can happen on mobile when the keyboard is open.

      val pageYOffset = dom.window.pageYOffset.toInt
      val windowHeight = dom.window.innerHeight.toInt
      val documentHeight = dom.document.documentElement.scrollHeight

      val s = bs.state.runNow()

      val scrollingUp = windowHeight == s.windowHeight &&
        documentHeight == s.documentHeight &&
        pageYOffset < s.pageYOffset

      val shortPage = windowHeight < 400

      val bottomVisiblePixel = pageYOffset + windowHeight
      val atEndOfPage = documentHeight - bottomVisiblePixel < 30

      val shouldCollapse = shortPage && !atEndOfPage && !scrollingUp

      bs.modState(
          _.copy(isCollapsed = shouldCollapse,
                 pageYOffset = pageYOffset,
                 windowHeight = windowHeight,
                 documentHeight = documentHeight))
        .runNow()

      recalcCollapseTimerId.foreach(id => dom.window.clearTimeout(id))
      // recalculate in 5 seconds, so scroll up will only last 2.5 seconds
      recalcCollapseTimerId = Some(dom.window.setTimeout(() => updateIsCollapsed(), 2500))
    }

    var recalcCollapseTimerId: Option[Int] = None

    val resizeListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 200) {
      (e: js.Dynamic) =>
        updateIsCollapsed()
    }

    val scrollListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 200) {
      (e: js.Dynamic) =>
        updateIsCollapsed()
    }

    val routeObserver: Observer[DoteRoute] = (route: DoteRoute) => {
      updateIsCollapsed()
    }

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {

      <.div(
        <.div(^.height := "64px"),
        <.div(
          ^.className := Styles.bottomFixedNavRoot,
          Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
            Grid(item = true, xs = 12)(
              Fader(in = s.isLoading)(LinearProgress()())
            ),
            Grid(item = true, xs = 12)(Divider()()),
            Grid(item = true, xs = 12)(
              Collapse(in = !s.isCollapsed)(BottomNavigation(onChange = (_, value) => {
                bs.modState(_.copy(navValue = value))
              })(
                BottomNavigationAction(icon = Icons.Home(),
                                       value = "home",
                                       onClick = doteRouterCtl.set(HomeRoute))(),
                BottomNavigationAction(
                  icon = Icons.Search(),
                  value = "search",
                  onClick = doteRouterCtl.set(SearchRoute)
                )(),
                BottomNavigationAction(
                  icon = Icons.AccountCircle(),
                  label = Typography(variant = Typography.Variants.Caption)(
                    if (s.loggedInPerson.isDefined) s.loggedInPerson.get.username else "Login"),
                  value = "profile",
                  showLabel = true,
                  onClick = handleProfileButtonClicked(p)
                )()
              ))
            )
          )
        )
      )
    }
  }

  private def navValueFromUrl(p: Props): String = {
    p.currentRoute match {
      case SearchRoute => "search"
      case ProfileRoute(_) => "profile"
      case _ => "home"
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(
      p =>
        State(navValue = navValueFromUrl(p),
              isLoading = GlobalLoadingManager.curState.isLoading,
              loggedInPerson = LoggedInPersonManager.person))
    .backend(new Backend(_))
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .componentWillMount(x => x.backend.onMount)
    .componentWillUnmount(x => x.backend.onUnmount)
    .build

  def apply(currentRoute: DoteRoute)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(currentRoute))
}
