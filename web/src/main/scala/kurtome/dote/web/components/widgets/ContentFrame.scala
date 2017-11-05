package kurtome.dote.web.components.widgets

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.{DoteRoute, DoteRouterCtl, HomeRoute}
import kurtome.dote.web.Styles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui.MuiThemeProvider.CreateMuiTheme

import scala.scalajs._

object ContentFrame {

  private val l$ = js.Dynamic.literal

  // Look at the documentation at https://material-ui-next.com/customization/themes/ for
  // that customization is possible.
  private val theme = l$(
    "typography" -> l$(
      "fontFamily" -> "syntesia",
      "display4" -> l$(
        "fontFamily" -> "jaapokkiSubtract"
      ),
      "title" -> l$(
        "fontFamily" -> "aileronHeavy"
      ),
      "headline" -> l$(
        "fontFamily" -> "aileronHeavy"
      ),
      "subheading" -> l$(
        "fontFamily" -> "aileronSemiBold"
      )
    )
  )

  case class Props(routerCtl: DoteRouterCtl)

  class Backend(bs: BackendScope[Props, Unit]) {
    def render(p: Props, pc: PropsChildren): VdomElement = {
      MuiThemeProvider(theme)(
        Grid(container = true, justify = Grid.Justify.Center)(
          Grid(item = true, md = 12, lg = 10)(
            Grid(container = true, justify = Grid.Justify.Center, spacing = 24)(
              Grid(item = true, xs = 12)(
                p.routerCtl.link(HomeRoute)(
                  ^.className := Styles.plainAnchor,
                  Typography(className = Styles.siteTitle,
                             typographyType = Typography.Type.Display4)("Pod Feels")
                )
              ),
              Grid(item = true, xs = 3)(),
              Grid(item = true, xs = 6)(Divider()()),
              Grid(item = true, xs = 3)(),
              Grid(item = true, xs = 12)(pc)
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
