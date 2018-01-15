package kurtome.dote.web.components.widgets.feed

import dote.proto.api.dotable.Dotable
import dote.proto.api.feed.{FeedDotableList => ApiList}
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.VdomElement
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.DoteRouterCtl
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.{ContentFrame, EntityTile}
import kurtome.dote.web.utils.MuiInlineStyleSheet
import kurtome.dote.web.utils.Debounce
import org.scalajs.dom

import scala.scalajs.js
import scalacss.ScalaCssReact._

object FeedDotableList {

  private object Styles extends StyleSheet.Inline with MuiInlineStyleSheet {
    import dsl._

    val tileContainer = style(
      marginTop(12 px)
    )
  }
  Styles.addToDocument()
  import Styles.richStyle

  case class Props(routerCtl: DoteRouterCtl, list: ApiList)
  case class State(tileSizePx: Int = 100)

  private val breakpointTileSizes = Map[String, Int](
    "xs" -> 125,
    "sm" -> 125,
    "md" -> 150,
    "lg" -> 175,
    "xl" -> 200
  )

  private def currentTileSizePx: Int =
    breakpointTileSizes(currentBreakpointString)

  class Backend(bs: BackendScope[Props, State]) {
    val updateTileSize: Callback = {
      bs.modState(_.copy(tileSizePx = currentTileSizePx))
    }

    val resizeListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 200) {
      (e: js.Dynamic) =>
        updateTileSize.runNow()
    }
    dom.window.addEventListener("resize", resizeListener)

    val handleWillUnmount: Callback = Callback {
      dom.window.removeEventListener("resize", resizeListener)
    }

    def render(p: Props, s: State): VdomElement = {
      val list = p.list.getList
      // the actual padding will be dynamic since the tiles are evenly spaced, so just make sure
      // the number of tiles has some extra space
      val tilePaddingPx = s.tileSizePx / 5
      val availableWidthPx = ContentFrame.innerWidthPx
      val numTilesPerRow: Int = availableWidthPx / (s.tileSizePx + tilePaddingPx)
      val numRows = if (numTilesPerRow < 6) 2 else 1
      val numTiles = numTilesPerRow * numRows
      val dotables = list.dotables
      // Pad with placeholders to make the list spacing balanced, rendering code below
      // will handle the placeholders
        .padTo(numTiles, Dotable.defaultInstance)
        // take the number that fit
        .take(numTiles)

      val minPaddingPx = 12
      val leftoverWidthPx = availableWidthPx - (numTilesPerRow * (s.tileSizePx + minPaddingPx))
      val tileWidthPx = s.tileSizePx + (leftoverWidthPx / numTilesPerRow)

      Grid(container = true, spacing = 0)(
        Grid(item = true, xs = 12)(
          Typography(typographyType = Typography.Type.SubHeading)(list.title),
          Grid(container = true,
               spacing = 0,
               alignItems = Grid.AlignItems.FlexStart,
               justify = Grid.Justify.SpaceBetween)(
            dotables.zipWithIndex map {
              case (dotable, i) =>
                Grid(key = Some(dotable.id + i), item = true, style = Styles.tileContainer.inline)(
                  if (dotable != Dotable.defaultInstance) {
                    EntityTile(p.routerCtl, dotable = dotable, width = asPxStr(tileWidthPx))()
                  } else {
                    // Placeholder for correct spacing
                    <.div(^.width := asPxStr(tileWidthPx))
                  }
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
    .componentWillUnmount(x => x.backend.handleWillUnmount)
    .build

  def apply(routeCtl: DoteRouterCtl, list: ApiList, key: Option[String] = None) = {
    if (key.isDefined) {
      component.withKey(key.get).withProps(Props(routeCtl, list))
    } else {
      component.withProps(Props(routeCtl, list))
    }
  }

}
