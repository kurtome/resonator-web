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
import kurtome.dote.web.constants.MuiTheme
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

    val title = style(
      width.unset
    )

    val titleContainer = style(
      width.unset
    )

    val actionsContainer = style(
      width.unset
    )

    val pageLengthHack = style(
      position.fixed,
      //width(100 %%),
      bottom(0 px)
    )

    val navWrapper = style(
      zIndex(1),
      position.fixed,
      width(100 %%),
      top(0 px)
    )

    val navPaper = style(
      backgroundColor :=! MuiTheme.theme.palette.primary.dark,
      width.unset,
      height.unset
    )

    val bottomNavRoot = style(
      position.absolute,
      marginTop(SharedStyles.spacingUnit),
      width(100 %%),
      bottom(0 px)
    )

    val invisible = style(
      visibility.hidden
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

    var recalcCollapseTimerId: Option[Int] = None

    val loadingObserver: Observer[GlobalLoadingManager.State] =
      (gs: GlobalLoadingManager.State) => {
        bs.modState(_.copy(isLoading = gs.isLoading)).runNow()
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

      val topVisiblePixel = pageYOffset
      val topOfPage = topVisiblePixel < 40

      val shouldCollapse = shortPage && !topOfPage && !scrollingUp

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

    val resizeListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 100) {
      (e: js.Dynamic) =>
        updateIsCollapsed()
    }

    val scrollListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 100) {
      (e: js.Dynamic) =>
        updateIsCollapsed()
    }

    val routeObserver: Observer[DoteRoute] = (route: DoteRoute) => {
      updateIsCollapsed()
    }

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {

      <.div(
        <.div(
          // placeholder to make the height match the nav bar when it's fixed to push the main
          // content out of the nav bar
          IconButton(style = Styles.invisible)(Icons.Search())
        ),
        <.div(
          ^.className := Styles.navWrapper,
          Collapse(in = !s.isCollapsed)(
            Paper(style = Styles.navPaper)(
              CenteredMainContent()(
                GridContainer(justify = Grid.Justify.SpaceBetween, spacing = 0)(
                  GridItem(hidden = Grid.HiddenProps(xsUp = s.isCollapsed))(SiteTitle()()),
                  GridItem()(
                    GridContainer(style = Styles.actionsContainer,
                                  spacing = 0,
                                  alignItems = Grid.AlignItems.Center)(
                      GridItem(hidden = Grid.HiddenProps(xsUp = LoggedInPersonManager.isLoggedIn))(
                        Button(onClick = doteRouterCtl.set(LoginRoute))("Login")),
                      GridItem(hidden = Grid.HiddenProps(xsUp = p.currentRoute == HomeRoute))(
                        IconButton(onClick = doteRouterCtl.set(HomeRoute))(Icons.Home())
                      ),
                      GridItem(hidden =
                        Grid.HiddenProps(xsUp = LoggedInPersonManager.isNotLoggedIn))(IconButton(
                        onClick = handleProfileButtonClicked(p))(Icons.AccountCircle())),
                      GridItem()(
                        IconButton(onClick = doteRouterCtl.set(SearchRoute))(Icons.Search()))
                    )
                  )
                )
              ))),
          Fader(in = s.isLoading)(LinearProgress()())
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
    .componentDidMount(x => x.backend.onMount)
    .componentWillUnmount(x => x.backend.onUnmount)
    .build

  def apply(currentRoute: DoteRoute)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(currentRoute))
}
