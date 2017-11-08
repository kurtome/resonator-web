package kurtome.dote.web.components.widgets

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.{AddRoute, DoteRoute, DoteRouterCtl, HomeRoute}
import kurtome.dote.web.InlineStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui.MuiThemeProvider.CreateMuiTheme

import scala.scalajs._

object ContentFrame {

  private val l$ = js.Dynamic.literal

  // Look at the documentation at https://material-ui-next.com/customization/themes/ for
  // that customization is possible.
  private val theme = l$(
    "palette" -> l$(
      "primary" -> Colors.blueGrey,
      "secondary" -> Colors.deepOrange,
      "light" -> l$(
        "background" -> l$(
          "default" -> "#e0ddc0",
          "paper" -> "#fff",
          "appBar" -> "#f5f5f5",
          "contentFrame" -> "#eeeeee"
        )
      )
    ),
    "typography" -> l$(
      "fontFamily" -> "syntesia",
      "display4" -> l$(
        "fontFamily" -> "jaapokkiSubtract",
        "textTransform" -> "uppercase"
      ),
      "display3" -> l$(
        "fontFamily" -> "jaapokkiSubtract",
        "textTransform" -> "uppercase"
      ),
      "display2" -> l$(
        "fontFamily" -> "jaapokkiSubtract",
        "textTransform" -> "uppercase"
      ),
      "display1" -> l$(
        "fontFamily" -> "jaapokkiSubtract",
        "textTransform" -> "uppercase"
      ),
      "title" -> l$(
        "fontFamily" -> "aileronHeavy",
        "textTransform" -> "uppercase"
      ),
      "headline" -> l$(
        "fontFamily" -> "aileronHeavy",
        "textTransform" -> "uppercase"
      ),
      "subheading" -> l$(
        "fontFamily" -> "aileronSemiBold",
        "textTransform" -> "uppercase"
      ),
      "button" -> l$(
        "fontFamily" -> "aileronRegular",
        "textTransform" -> "uppercase"
      )
    )
  )

  case class Props(routerCtl: DoteRouterCtl)

  class Backend(bs: BackendScope[Props, Unit]) {

    def render(p: Props, mainContent: PropsChildren): VdomElement = {
      MuiThemeProvider(theme)(
        <.div(
          Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
            Grid(item = true, xs = 12)(
              Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
                Grid(item = true, xs = 12)(
                  Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
                    Grid(item = true, xs = 8, md = 6, lg = 4, xl = 2)(
                      p.routerCtl.link(HomeRoute)(
                        ^.className := InlineStyles.plainAnchor,
                        Typography(className = InlineStyles.siteTitle,
                                   typographyType = Typography.Type.Display1)("PodFeels")
                      )
                    )
                  )
                ),
                Grid(item = true, xs = 12)(
                  Grid(container = true, justify = Grid.Justify.Center, spacing = 0)(
                    Grid(item = true, xs = 8, md = 6, lg = 4, xl = 2)(Divider()())
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
                )
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
