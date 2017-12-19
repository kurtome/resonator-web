package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.shared.util.observer._
import kurtome.dote.web.utils.GlobalLoadingManager
import org.scalajs.dom
import wvlet.log.LogSupport

import scalacss.internal.mutable.StyleSheet

/**
  * Navigation controls at the bottom of each page.
  */
object NavBar extends LogSupport {

  case class Props(routerCtl: DoteRouterCtl)
  case class State(navValue: String = "home", isLoading: Boolean = false)

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
    val stateObserver: Observer[GlobalLoadingManager.State] = (gs: GlobalLoadingManager.State) => {
      debug(s"global state changed to: $gs")
      bs.modState(_.copy(isLoading = gs.isLoading)).runNow()
      bs.forceUpdate.runNow()
    }
    debug("adding observer")
    GlobalLoadingManager.stateObservable.addObserver(stateObserver)
    val onUnmount: Callback = Callback {
      debug("removing observer")
      GlobalLoadingManager.stateObservable.removeObserver(stateObserver)
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
            BottomNavigation(value = s.navValue, onChange = (_, value) => {
              bs.modState(_.copy(navValue = value))
            })(
              BottomNavigationButton(icon = Icons.Home(),
                                     value = "home",
                                     onClick = p.routerCtl.set(HomeRoute))(),
              BottomNavigationButton(icon = Icons.Add(),
                                     value = "add",
                                     onClick = p.routerCtl.set(AddRoute))(),
              BottomNavigationButton(icon = Icons.Search(),
                                     value = "search",
                                     onClick = p.routerCtl.set(SearchRoute))(),
              BottomNavigationButton(icon = Icons.AccountCircle(), value = "account")()
            ))
        )
      )
    }
  }

  private def navValueFromUrl: String = {
    if (dom.window.location.hash.startsWith("#/add")) {
      "add"
    } else {
      "home"
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(
      State(navValue = navValueFromUrl, isLoading = GlobalLoadingManager.curState.isLoading))
    .backend(new Backend(_))
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .componentWillUnmount(x => x.backend.onUnmount)
    .build

  def apply(routerCtl: DoteRouterCtl)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(routerCtl))
}
