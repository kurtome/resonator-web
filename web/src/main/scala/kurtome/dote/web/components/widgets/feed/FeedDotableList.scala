package kurtome.dote.web.components.widgets.feed

import dote.proto.api.feed.FeedDotableList
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.VdomElement
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.DoteRouterCtl
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.EntityTile
import kurtome.dote.web.utils.MuiInlineStyleSheet

import scalacss.ScalaCssReact._

object FeedDotableList {

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val tileContainer = style(
      marginTop(12 px),
      marginRight(24 px)
    )
  }
  val muiStyles = new MuiInlineStyleSheet(Styles)
  import muiStyles._

  case class Props(routerCtl: DoteRouterCtl, list: FeedDotableList)
  case class State()

  class Backend(bs: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      val list = p.list.getList
      Grid(container = true, spacing = 0)(
        Grid(item = true, xs = 12)(
          Typography(typographyType = Typography.Type.SubHeading)(list.title),
          Grid(container = true,
               spacing = 0,
               alignItems = Grid.AlignItems.FlexStart,
               justify = Grid.Justify.FlexStart)(
            list.dotables map { dotable =>
              Grid(key = Some(dotable.id), item = true, style = Styles.tileContainer.inline)(
                EntityTile(p.routerCtl, dotable = dotable)()
              )
            } toVdomArray
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build

  def apply(routeCtl: DoteRouterCtl, list: FeedDotableList) =
    component.withProps(Props(routeCtl, list))

}
