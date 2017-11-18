package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.{AddRoute, DoteRouterCtl, HomeRoute}
import kurtome.dote.web.InlineStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.constants.{MuiTheme, StringValues}
import org.scalajs.dom

object ContentFrame {

  case class Props(routerCtl: DoteRouterCtl)
  case class State(navValue: String = "home")

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
                         md = 8,
                         lg = 6,
                         xl = 4,
                         className = InlineStyles.siteTitleContainer)(
                      p.routerCtl.link(HomeRoute)(
                        ^.className := InlineStyles.siteTitleAnchor,
                        <.span(^.className := InlineStyles.siteTitleText)(StringValues.siteTitle)
                      ),
                      <.span(^.className := InlineStyles.underConstructionText)(
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
                    Grid(item = true,
                         xs = 12,
                         md = 10,
                         lg = 8,
                         xl = 6,
                         className = InlineStyles.contentRoot)(mainContent)
                  )
                ),
                // Add a div with fixed height so that when scrolling to the bottom of the page, the
                // content above never gets stuck under the bottom nav, which is fixed width
                Grid(item = true, xs = 12)(<.div(^.minHeight := "80px")),
              )
            )
          ),
          <.div(
            ^.className := InlineStyles.bottomNavRoot,
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
