package kurtome.dote.web.components.widgets

import dote.proto.api.person.Person
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.shared.util.observer._
import kurtome.dote.web.utils.LoggedInPersonManager.LoginState
import kurtome.dote.web.utils.{GlobalLoadingManager, LoggedInPersonManager}
import org.scalajs.dom
import wvlet.log.LogSupport

import scalacss.internal.mutable.StyleSheet

/**
  * Navigation controls at the bottom of each page.
  */
object NavBar extends LogSupport {

  case class Props(routerCtl: DoteRouterCtl)
  case class State(navValue: String = "home",
                   isLoading: Boolean = false,
                   loginDialogOpen: Boolean = false,
                   loggedInPerson: Option[Person] = None)

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val bottomNavRoot = style(
      position.fixed,
      width(100 %%),
      bottom(0 px)
    )
  }
  Styles.addToDocument()

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {
    val loadingObserver: Observer[GlobalLoadingManager.State] =
      (gs: GlobalLoadingManager.State) => {
        bs.modState(_.copy(isLoading = gs.isLoading)).runNow()
        bs.forceUpdate.runNow()
      }

    val loginObserver: Observer[LoginState] = (ls: LoginState) => {
      bs.modState(_.copy(loggedInPerson = ls.person)).runNow()
      bs.forceUpdate.runNow()
    }

    GlobalLoadingManager.stateObservable.addObserver(loadingObserver)
    LoggedInPersonManager.stateObservable.addObserver(loginObserver)

    val onUnmount: Callback = Callback {
      GlobalLoadingManager.stateObservable.removeObserver(loadingObserver)
      LoggedInPersonManager.stateObservable.removeObserver(loginObserver)
    }

    val handleAccountButtonClicked: Callback =
      bs.modState(_.copy(loginDialogOpen = true))

    val handleLoginDialogClosed: Callback = Callback {
      bs.modState(_.copy(loginDialogOpen = false)).runNow()
    }

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {
      <.div(
        ^.className := Styles.bottomNavRoot,
        Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
          Grid(item = true, xs = 12)(
            Fade(in = s.isLoading, timeoutMs = 1000)(LinearProgress()())
          ),
          Grid(item = true, xs = 12)(Divider()()),
          Grid(item = true, xs = 12)(
            BottomNavigation(onChange = (_, value) => {
              bs.modState(_.copy(navValue = value))
            })(
              BottomNavigationButton(icon = Icons.Home(),
                                     value = "home",
                                     onClick = p.routerCtl.set(HomeRoute))(),
              BottomNavigationButton(icon = Icons.Add(),
                                     value = "add",
                                     onClick = p.routerCtl.set(AddRoute))(),
              BottomNavigationButton(
                icon = Icons.Search(),
                value = "search",
                onClick = p.routerCtl.set(SearchRoute)
              )(),
              BottomNavigationButton(
                icon = Icons.AccountCircle(),
                label = Typography(typographyType = Typography.Type.Caption)(
                  if (s.loggedInPerson.isDefined) s.loggedInPerson.get.username else "Login"),
                value = "account",
                showLabel = true,
                onClick = handleAccountButtonClicked
              )()
            ))
        ),
        LoginDialog(routerCtl = p.routerCtl,
                    open = s.loginDialogOpen,
                    loggedInPerson = s.loggedInPerson,
                    onClose = handleLoginDialogClosed)()
      )
    }
  }

  private def navValueFromUrl: String = {
    if (dom.window.location.hash.startsWith("#/search")) {
      "search"
    } else if (dom.window.location.hash.startsWith("#/account")) {
      "account"
    } else if (dom.window.location.hash.startsWith("#/add")) {
      "add"
    } else {
      "home"
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(
      State(navValue = navValueFromUrl,
            isLoading = GlobalLoadingManager.curState.isLoading,
            loggedInPerson = LoggedInPersonManager.curState.person))
    .backend(new Backend(_))
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .componentWillUnmount(x => x.backend.onUnmount)
    .build

  def apply(routerCtl: DoteRouterCtl, loginCode: Option[String] = None)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(routerCtl))
}
