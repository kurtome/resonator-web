package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.util.observer.Observer
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.constants.MuiTheme
import scalacss.internal.mutable.StyleSheet
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.constants.MuiTheme.Theme
import kurtome.dote.web.utils.BaseBackend
import org.scalajs.dom
import wvlet.log.LogSupport

/**
  * Pager wrapper includes header/footer and renders child content within a centered portion of the
  * screen.
  */
object ContentFrame extends LogSupport {

  val drawerWidth = 240

  object Styles extends StyleSheet.Inline {
    import dsl._

    val bottomNavRoot = style(
      position.fixed,
      width(100 %%),
      bottom(0 px)
    )

  }

  case class Props(currentRoute: DoteRoute)
  case class State(theme: Theme, drawerOpen: Boolean = false)

  /**
    * Width of the main content area (based on the current viewport size).
    */
  def innerWidthPx: Int = {
    // Based on the grid sizing of the mainContent wrapper
    val usableRatio = ComponentHelpers.currentBreakpointString match {
      case "xl" => 6.0 / 12.0
      case "lg" => 8.0 / 12.0
      case "md" => 10.0 / 12.0
      case "sm" => 10.0 / 12.0
      case _ => 12.0 / 12.0
    }

    val paddingPx = 32

    Math
      .round(dom.window.document.body.offsetWidth * usableRatio)
      .toInt - (paddingPx + drawerWidth)
  }

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val stateObserver: Observer[Theme] = (theme: Theme) => {
      bs.modState(_.copy(theme = theme)).runNow()
    }
    MuiTheme.stateObservable.addObserver(stateObserver)
    val handleUnmount: Callback = Callback {
      MuiTheme.stateObservable.removeObserver(stateObserver)
    }

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {
      MuiThemeProvider(s.theme)(
        <.div(
          ^.position.relative,
          ^.display.flex,
          ^.minHeight := "100vh",
          NavBar(p.currentRoute,
                 onMenuClick = bs.modState(s => s.copy(drawerOpen = !s.drawerOpen)))(),
          MenuDrawer(open = s.drawerOpen, onClose = bs.modState(_.copy(drawerOpen = false)))(),
          <.main(
            ^.position.relative,
            ^.flexGrow := "1",
            Toolbar()(), // leave space for the navbar
            mainContent
          ),
          NotificationSnackBar()(),
          AudioControls()()
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State(MuiTheme.theme))
    .backend(new Backend(_))
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .componentWillUnmount(x => x.backend.handleUnmount)
    .build

  def apply(currentRoute: DoteRoute)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(currentRoute))
}
