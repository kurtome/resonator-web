package kurtome.dote.web.components.widgets

import kurtome.dote.proto.api.person.Person
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.util.observer.Observer
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.{GlobalLoadingManager, LoggedInPersonManager}
import wvlet.log.LogSupport

import scalacss.internal.mutable.StyleSheet

/**
  * Navigation controls at the bottom of each page.
  */
object NavBar extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val bottomNavRoot = style(
      position.fixed,
      width(100 %%),
      bottom(0 px)
    )
  }

  case class Props(currentRoute: DoteRoute)
  case class State(navValue: String = "home",
                   isLoading: Boolean = false,
                   loggedInPerson: Option[Person] = None)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val loadingObserver: Observer[GlobalLoadingManager.State] =
      (gs: GlobalLoadingManager.State) => {
        bs.modState(_.copy(isLoading = gs.isLoading)).runNow()
        bs.forceUpdate.runNow()
      }

    GlobalLoadingManager.stateObservable.addObserver(loadingObserver)

    val onUnmount: Callback = Callback {
      GlobalLoadingManager.stateObservable.removeObserver(loadingObserver)
    }

    val handleProfileButtonClicked = (p: Props) =>
      if (LoggedInPersonManager.isLoggedIn) {
        doteRouterCtl.set(ProfileRoute(LoggedInPersonManager.person.get.username))
      } else {
        doteRouterCtl.set(LoginRoute)
    }

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {
      <.div(
        ^.className := Styles.bottomNavRoot,
        Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
          Grid(item = true, xs = 12)(
            Fader(in = s.isLoading)(LinearProgress()())
          ),
          Grid(item = true, xs = 12)(Divider()()),
          Grid(item = true, xs = 12)(
            BottomNavigation(onChange = (_, value) => {
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
    .componentWillUnmount(x => x.backend.onUnmount)
    .build

  def apply(currentRoute: DoteRoute)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(currentRoute))
}
