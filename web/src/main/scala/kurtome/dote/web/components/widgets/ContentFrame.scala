package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.{AddRoute, DoteRouterCtl, HomeRoute}
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.audio.AudioPlayer
import kurtome.dote.web.audio.AudioPlayer.PlayerStatuses
import kurtome.dote.web.components.ComponentHelpers
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.constants.{MuiTheme, StringValues}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSON
import scalacss.internal.mutable.StyleSheet
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.utils.MuiInlineStyleSheet

/**
  * Pager wrapper includes header/footer and renders child content within a centered portion of the
  * screen.
  */
object ContentFrame {

  case class Props(routerCtl: DoteRouterCtl)
  case class State(navValue: String = "home")

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val siteTitleContainer = style(
      margin.auto,
      display.block,
      textAlign.center,
      paddingTop(30 px)
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
    Math.round(dom.window.innerWidth * usableRatio).toInt - paddingPx
  }

  class Backend(bs: BackendScope[Props, State]) {

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {
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
                         className = Styles.siteTitleContainer)(
                      p.routerCtl.link(HomeRoute)(
                        ^.className := SharedStyles.siteTitleAnchor,
                        <.span(^.className := SharedStyles.siteTitleText)(StringValues.siteTitle)
                      ),
                      <.span(^.className := SharedStyles.underConstructionText)(
                        "under construction")
                    )
                  )
                ),
                Grid(item = true, xs = 12)(
                  Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
                    Grid(item = true, xs = 10, md = 8, lg = 6, xl = 4)(Divider()())
                  )
                ),
                Grid(item = true, xs = 12)(
                  Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
                    Grid(item = true, xs = 12, md = 8, lg = 6, xl = 4)(
                      SearchBox(p.routerCtl)()
                    )
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
          <.div(
            ^.className := Styles.bottomNavRoot,
            Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
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
                  BottomNavigationButton(icon = Icons.AccountCircle(), value = "account")()
                ))
            )
          )
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
    .initialState(State(navValue = navValueFromUrl))
    .backend(new Backend(_))
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .build

  def apply(p: Props)(c: CtorType.ChildArg*) = component.withChildren(c: _*)(p)
}
