package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.{AddRoute, DoteRouterCtl, HomeRoute}
import kurtome.dote.web.InlineStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.constants.{MuiTheme, StringValues}

object ContentFrame {

  case class Props(routerCtl: DoteRouterCtl)

  class Backend(bs: BackendScope[Props, Unit]) {

    def render(p: Props, mainContent: PropsChildren): VdomElement = {
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
                        ^.className := InlineStyles.plainAnchor,
                        <.span(^.className := InlineStyles.siteTitleText)(StringValues.siteTitle)
                      )
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
                Grid(item = true, xs = 12)(<.div(^.minHeight := "80px")),
              )
            )
          ),
          <.div(
            ^.className := InlineStyles.bottomNavRoot,
            Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
              Grid(item = true, xs = 12)(Divider()()),
              Grid(item = true, xs = 12)(BottomNavigation()(
                BottomNavigationButton(icon = Icons.Home(),
                                       onClick = p.routerCtl.set(HomeRoute))(),
                BottomNavigationButton(icon = Icons.Add(), onClick = p.routerCtl.set(AddRoute))(),
                BottomNavigationButton(icon = Icons.AccountCircle())()
              ))
            )
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderPC((b, p, pc) => b.backend.render(p, pc))
    .build

  def apply(p: Props)(c: CtorType.ChildArg*) = component.withChildren(c: _*)(p)
}
