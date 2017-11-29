package kurtome.dote.web.components.widgets.feed

import dote.proto.api.feed.FeedDotableList
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.VdomElement
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.DoteRouterCtl
import kurtome.dote.web.components.ComponentHelpers
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.{ContentFrame, EntityTile}
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.MuiInlineStyleSheet
import kurtome.dote.web.utils.Debounce
import org.scalajs.dom

import scala.scalajs.js
import scalacss.ScalaCssReact._

object FeedDotableList {

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val tileContainer = style(
      marginTop(12 px),
      //marginRight(tileMarginRight px)
    )
  }
  Styles.addToDocument()
  val muiStyles = new MuiInlineStyleSheet(Styles)
  import muiStyles._

  case class Props(routerCtl: DoteRouterCtl, list: FeedDotableList)
  case class State(tileSizePx: Int = 100)

  private val breakpointTileSizes = Map[String, Int](
    "xs" -> 65,
    "sm" -> 90,
    "md" -> 125,
    "lg" -> 140,
    "xl" -> 160
  )

  private def currentTileSizePx: Int =
    breakpointTileSizes(ComponentHelpers.currentBreakpointString)

  class Backend(bs: BackendScope[Props, State]) {
    val updateTileSize: Callback = {
      bs.modState(_.copy(tileSizePx = currentTileSizePx))
    }

    val resizeListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 200) {
      (e: js.Dynamic) =>
        updateTileSize.runNow()
    }
    dom.window.addEventListener("resize", resizeListener)

    val onUnmount: Callback = Callback {
      println("removing listener")
      dom.window.removeEventListener("resize", resizeListener)
    }

    def render(p: Props, s: State): VdomElement = {
      val list = p.list.getList
      // the actual padding will be dynamic since the tiles are evenly spaced, so just make sure
      // the number of tiles has some extra space
      val tilePaddingPx = s.tileSizePx / 5
      val numTiles: Int = ContentFrame.innerWidthPx / (s.tileSizePx + tilePaddingPx)
      val dotables = list.dotables.take(numTiles)

      Grid(container = true, spacing = 0)(
        Grid(item = true, xs = 12)(
          Typography(typographyType = Typography.Type.SubHeading)(list.title),
          Grid(container = true,
               spacing = 0,
               wrap = Grid.Wrap.NoWrap,
               alignItems = Grid.AlignItems.FlexStart,
               justify = Grid.Justify.SpaceBetween)(
            dotables map { dotable =>
              Grid(key = Some(dotable.id), item = true, style = Styles.tileContainer.inline)(
                EntityTile(p.routerCtl,
                           dotable = dotable,
                           width = s.tileSizePx + "px",
                           height = "auto")()
              )
            } toVdomArray
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State(tileSizePx = currentTileSizePx))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .componentWillUnmount(x => x.backend.onUnmount)
    .build

  def apply(routeCtl: DoteRouterCtl, list: FeedDotableList, key: Option[String] = None) = {
    if (key.isDefined) {
      component.withKey(key.get).withProps(Props(routeCtl, list))
    } else {
      component.withProps(Props(routeCtl, list))
    }
  }

}
