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
import kurtome.dote.web.utils.{GlobalLoadingManager, LoggedInPersonManager}
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js
import scalacss.internal.mutable.StyleSheet

/**
  * Navigation controls at the bottom of each page.
  */
object NavBar extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val actionsContainer = style(
      width.unset
    )

    val pageLengthHack = style(
      position.fixed,
      bottom(0 px)
    )

    val navWrapper = style(
      zIndex(MuiTheme.theme.zIndex.drawer + 1),
      position.fixed,
      width(100 %%),
      top(0 px)
    )

    val appBar = style(
      zIndex(MuiTheme.theme.zIndex.drawer + 2),
      backgroundColor :=! MuiTheme.theme.palette.primary.dark
    )

    val appBarSlideIn = style(
      zIndex(MuiTheme.theme.zIndex.drawer + 2),
      backgroundColor :=! MuiTheme.theme.palette.primary.dark,
      animation := s"${Animations.slideIn.name.value} 0.5s",
      top(0 px)
    )

    val appBarSlideOut = style(
      zIndex(MuiTheme.theme.zIndex.drawer + 2),
      backgroundColor :=! MuiTheme.theme.palette.primary.dark,
      animation := s"${Animations.slideOut.name.value} 0.5s",
      top(-100 px)
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

    val actionButton = style(
      color.white
    )

    val menuIconContainer = style(
      marginRight(16 px)
    )
  }

  private object Animations extends StyleSheet.Inline {
    import dsl._
    val slideIn = keyframes(
      (0 %%) -> keyframe(top(-100 px)),
      (100 %%) -> keyframe(top(0 px))
    )

    val slideOut = keyframes(
      (0 %%) -> keyframe(top(0 px)),
      (100 %%) -> keyframe(top(-100 px))
    )
  }
  Animations.addToDocument()

  case class Props(currentRoute: DoteRoute, onMenuClick: Callback)
  case class State(isCollapsed: Boolean = false,
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

    var initialStyle = true
    private def appBarStyle(s: State): StyleA = {
      if (initialStyle && !s.isCollapsed) {
        Styles.appBar
      } else {
        if (s.isCollapsed) {
          Styles.appBarSlideOut
        } else {
          Styles.appBarSlideIn
        }
      }
    }

    private def renderToolbarContent(p: Props, s: State): VdomNode = {
      GridContainer(justify = Grid.Justify.SpaceBetween,
                    alignItems = Grid.AlignItems.Center,
                    spacing = 0)(
        GridItem()(
          GridContainer(spacing = 0, alignItems = Grid.AlignItems.Center)(
            Hidden(mdUp = true)(
              GridItem(style = Styles.menuIconContainer)(
                IconButton(onClick = p.onMenuClick, style = Styles.actionButton)(Icons.Menu()))),
            GridItem()(SiteTitle(SiteTitle.Colors.Light)())
          )
        ),
        GridItem()(
          GridContainer(style = Styles.actionsContainer,
                        spacing = 0,
                        alignItems = Grid.AlignItems.Center)(
            Hidden(xsUp = LoggedInPersonManager.isLoggedIn)(
              GridItem()(Button(style = Styles.actionButton,
                                onClick = doteRouterCtl.set(LoginRoute))("Log In"))),
            Hidden(xsUp = LoggedInPersonManager.isNotLoggedIn)(
              GridItem()(
                IconButton(style = Styles.actionButton, onClick = handleProfileButtonClicked(p))(
                  Icons.AccountCircle()))),
            Hidden(xsDown = true)(
              GridItem()(
                IconButton(style = Styles.actionButton,
                           onClick = doteRouterCtl.set(SearchRoute()))(Icons.Search()))
            )
          )
        )
      )

    }

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {

      AppBar(position = AppBar.Positions.Fixed,
             color = AppBar.Colors.Inherit,
             style = appBarStyle(s))(
        Toolbar(disableGutters = true)(
          Hidden(smDown = true)(
            <.div(
              ^.paddingLeft := asPxStr(ContentFrame.drawerWidth),
              ^.width := "100%",
              CenteredMainContent()(renderToolbarContent(p, s))
            )
          ),
          Hidden(mdUp = true)(
            <.div(
              ^.width := "100%",
              CenteredMainContent()(renderToolbarContent(p, s))
            )
          )
        )
        //Fader(in = s.isLoading)(LinearProgress()())
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p =>
      State(isLoading = GlobalLoadingManager.curState.isLoading,
            loggedInPerson = LoggedInPersonManager.person))
    .backend(new Backend(_))
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .componentDidMount(x => x.backend.onMount)
    .componentWillUnmount(x => x.backend.onUnmount)
    .build

  def apply(currentRoute: DoteRoute, onMenuClick: Callback = Callback.empty)(
      c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(currentRoute, onMenuClick))
}
