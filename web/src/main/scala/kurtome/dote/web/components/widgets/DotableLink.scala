package kurtome.dote.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.views.DotableDetailView
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.rpc.TimeCachedValue

object DotableLink {

  case class Props(dotable: Dotable)

  class Backend(bs: BackendScope[Props, Unit]) {
    def render(p: Props, pc: PropsChildren): VdomElement = {
      val color = MuiTheme.theme.palette.primary.light
      val route = DetailsRoute(p.dotable.id, p.dotable.slug)
      val url = doteRouterCtl.urlFor(route)
      <.a(
        ^.href := url.value,
        ^.onClick ==> ((e) =>
          Callback {
            DotableDetailView.cachedDotable = TimeCachedValue.minutes(1, p.dotable)
            e.preventDefault()
            doteRouterCtl.set(route).runNow()
          }),
        ^.textDecoration := "none",
        ^.color := color,
        pc
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderPC((builder, props, pc) => builder.backend.render(props, pc))
    .build

  def apply(dotable: Dotable)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*).withProps(Props(dotable))()
}
