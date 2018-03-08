package kurtome.dote.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.vdom.VdomElement
import kurtome.dote.web.DoteRoutes._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.constants.MuiTheme

object SiteLink {

  case class Props(route: DoteRoute)

  class Backend(bs: BackendScope[Props, Unit]) {
    def render(p: Props, pc: PropsChildren): VdomElement = {
      val color = if (MuiTheme.theme.palette.paletteType == "light") {
        MuiTheme.theme.palette.primary.dark
      } else {
        MuiTheme.theme.palette.primary.light
      }
      doteRouterCtl.link(p.route)(^.color := color, pc)
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderPC((builder, props, pc) => builder.backend.render(props, pc))
    .build

  def apply(route: DoteRoute)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*).withProps(Props(route))()
}
