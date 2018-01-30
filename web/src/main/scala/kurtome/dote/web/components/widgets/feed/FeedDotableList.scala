package kurtome.dote.web.components.widgets.feed

import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.feed.{FeedDotableList => ApiList}
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.VdomElement
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.DoteRouterCtl
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.utils.MuiInlineStyleSheet
import kurtome.dote.web.utils.Debounce
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js
import scalacss.ScalaCssReact._

object FeedDotableList extends LogSupport {

  private object Styles extends StyleSheet.Inline with MuiInlineStyleSheet {
    import dsl._

    val tileContainer = style(
      marginTop(12 px)
    )
  }
  Styles.addToDocument()
  import Styles.richStyle

  case class Props(routerCtl: DoteRouterCtl, list: ApiList)
  case class State(tileSizePx: Int, availableWidthPx: Int)

  private val breakpointTileSizes = Map[String, Int](
    "xs" -> 125,
    "sm" -> 125,
    "md" -> 150,
    "lg" -> 175,
    "xl" -> 200
  )

  private val episodeTileSizes = Map[String, Int](
    "xs" -> 240,
    "sm" -> 240,
    "md" -> 260,
    "lg" -> 300,
    "xl" -> 350
  )

  private def currentTileSizePx(p: Props): Int = {
    p.list.list.get.dotables.head.kind match {
      case Dotable.Kind.PODCAST_EPISODE => episodeTileSizes(currentBreakpointString)
      case _ => breakpointTileSizes(currentBreakpointString)
    }
  }

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {
    val updateTileSize: Callback = {
      val p = bs.props.runNow()
      debug(s"updating tile size ${p.list.getList.title}")
      bs.modState(
        _.copy(tileSizePx = currentTileSizePx(p), availableWidthPx = ContentFrame.innerWidthPx))
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

      val requestedWidth = s.tileSizePx
      val list = p.list.getList
      // the actual padding will be dynamic since the tiles are evenly spaced, so just make sure
      // the number of tiles has some extra space
      val tilePaddingPx = requestedWidth / 5
      // this calculation assumes all the tiles will be the same width (i.e. the same type of
      // dotable)
      val numTilesPerRow: Int = s.availableWidthPx / (requestedWidth + tilePaddingPx)
      val numRows =
        if (numTilesPerRow == 1) {
          3
        } else if (numTilesPerRow < 6) {
          2
        } else {
          1
        }
      val numTiles = numTilesPerRow * numRows
      val dotables = list.dotables
      // Pad with placeholders to make the list spacing balanced, rendering code below
      // will handle the placeholders
        .padTo(numTiles, Dotable.defaultInstance)
        // take the number that fit
        .take(numTiles)

      val minPaddingPx = 12
      val leftoverWidthPx = s.availableWidthPx - (numTilesPerRow * (requestedWidth + minPaddingPx))
      val tileWidthPx = requestedWidth + (leftoverWidthPx / numTilesPerRow)

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
                  if (dotable.kind == Dotable.Kind.PODCAST) {
                    PodcastTile(p.routerCtl, dotable = dotable, width = asPxStr(tileWidthPx))()
                  } else if (dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
                    EpisodeTile(p.routerCtl, dotable = dotable, width = tileWidthPx)()
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
    .initialStateFromProps(p =>
      State(tileSizePx = currentTileSizePx(p), availableWidthPx = ContentFrame.innerWidthPx))
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
