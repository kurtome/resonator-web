package kurtome.dote.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.vdom.VdomElement
import kurtome.dote.web.DoteRoutes._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._

import scala.scalajs.js

object SiteLink {

  case class Props(route: DoteRoute)

  class Backend(bs: BackendScope[Props, Unit]) {
    def render(p: Props, pc: PropsChildren): VdomElement = {
      doteRouterCtl.link(p.route)(^.className := SharedStyles.siteLink, pc)
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
