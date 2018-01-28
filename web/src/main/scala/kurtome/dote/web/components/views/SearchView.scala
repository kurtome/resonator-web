package kurtome.dote.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.DoteRouterCtl
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.{ContentFrame, SearchBox}
import wvlet.log.LogSupport

object SearchView extends LogSupport {

  case class Props(routerCtl: DoteRouterCtl)

  case class State()

  class Backend(bs: BackendScope[Props, State]) {

    def render(p: Props, s: State): VdomElement = {
      ContentFrame(p.routerCtl)(
        Grid(container = true, spacing = 0, justify = Grid.Justify.Center)(
          Grid(item = true, xs = 12, sm = 10, md = 8)(
            SearchBox(p.routerCtl)()
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((b, p, s) => b.backend.render(p, s))
    .build

  def apply(routerCtl: DoteRouterCtl) = {
    component.withProps(Props(routerCtl))
  }
}
