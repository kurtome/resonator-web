package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.{SharedStyles, WebMain}
import kurtome.dote.web.components.ComponentHelpers
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.constants.{MuiTheme, StringValues}
import org.scalajs.dom

import scalacss.internal.mutable.StyleSheet
import kurtome.dote.web.CssSettings._
import wvlet.log.LogSupport

import scala.scalajs.js

/**
  * Pager wrapper includes header/footer and renders child content within a centered portion of the
  * screen.
  */
object ContentFrame extends LogSupport {

  case class Props(routerCtl: DoteRouterCtl)
  case class State()

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val siteTitleText = styleF.bool(
      isXs =>
        styleS(
          fontFamily(SharedStyles.jaapokkiSubtractFf),
          fontSize(if (isXs) 1.5 rem else 3 rem),
          textAlign.center,
          color(Color(MuiTheme.theme.palette.text.primary.toString))
      )
    )

    val siteTitleContainer = styleF.bool(
      isXs =>
        styleS(
          margin.auto,
          display.block,
          textAlign.center,
          paddingTop(if (isXs) 4 px else 30 px)
      )
    )

    val underConstructionText = styleF.bool(
      isXs =>
        styleS(
          transform := "rotate(-20deg)",
          fontSize(if (isXs) 1 rem else 2 rem),
          left(35 %%),
          position.absolute,
          color(Color(MuiTheme.theme.palette.text.secondary.toString))
      )
    )

    val bottomNavRoot = style(
      position.fixed,
      width(100 %%),
      bottom(0 px)
    )
  }
  Styles.addToDocument()

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
    Math.round(WebMain.getRootNode.clientWidth * usableRatio).toInt - paddingPx
  }

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {
      val isXs = currentBreakpointString == "xs"

      MuiThemeProvider(MuiTheme.theme)(
        <.div(
          Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
            Grid(item = true, xs = 12)(
              Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
                Grid(item = true, xs = 12)(
                  Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
                    Grid(item = true,
                         xs = 10,
                         sm = 8,
                         md = 8,
                         lg = 6,
                         xl = 4,
                         className = Styles.siteTitleContainer(isXs))(
                      <.span(^.className := Styles.underConstructionText(isXs))(
                        "under construction"),
                      p.routerCtl.link(HomeRoute)(
                        ^.className := SharedStyles.siteTitleAnchor,
                        <.span(^.className := Styles.siteTitleText(isXs))(StringValues.siteTitle)
                      )
                    )
                  )
                ),
                Grid(item = true, xs = 12)(
                  Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
                    Grid(item = true, xs = 8, md = 6, lg = 4, xl = 4)(Divider()())
                  )
                ),
                Grid(item = true, xs = 12)(
                  Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
                    Grid(item = true,
                         xs = 12,
                         sm = 10,
                         md = 10,
                         lg = 8,
                         xl = 6,
                         className = SharedStyles.contentRoot)(mainContent)
                  )
                ),
                Grid(item = true, xs = 12)(<.div(^.minHeight := "80px", AudioWave())),
                // Add a div with fixed height so that when scrolling to the bottom of the page, the
                // content above never gets stuck under the bottom nav, which is fixed width
                Grid(item = true, xs = 12)(<.div(^.minHeight := "80px"))
              ),
              AudioControls(p.routerCtl)()
            )
          ),
          NotificationSnackBar(p.routerCtl)(),
          NavBar(p.routerCtl)()
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .build

  def apply(routerCtl: DoteRouterCtl)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(routerCtl))
}
