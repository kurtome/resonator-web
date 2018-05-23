package kurtome.dote.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.lib.LazyLoad
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.Debounce
import org.scalajs.dom
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

import scala.scalajs.js

object CompactItemList extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val tileContainer = style(
      marginTop(12 px)
    )

    val itemsContainer = style(
      width(100 %%),
      marginLeft(0 px),
      marginRight(0 px),
      marginBottom(8 px)
    )
  }
  Styles.addToDocument()

  case class Props(itemsPerRowBreakpoints: Map[String, Int],
                   title: String,
                   moreRoute: Option[DoteRoute],
                   padEmptyRows: Boolean)
  case class State(breakpoint: String, pageIndex: Int)

  private def fallbackItemsPerRow(breakpoint: String): Int = {
    breakpoint match {
      case "xs" => 1
      case "sm" => 2
      case "md" => 2
      case _ => 3
    }
  }

  private def calcItemsPerRow(p: Props, s: State): Int = {
    p.itemsPerRowBreakpoints.getOrElse(s.breakpoint, fallbackItemsPerRow(s.breakpoint))
  }

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val updateTileSize: Callback = {
      val p = bs.props.runNow()
      bs.modState(_.copy(breakpoint = currentBreakpointString, pageIndex = 0))
    }

    val resizeListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 200) {
      (e: js.Dynamic) =>
        updateTileSize.runNow()
    }
    dom.window.addEventListener("resize", resizeListener)

    val handleWillUnmount: Callback = Callback {
      dom.window.removeEventListener("resize", resizeListener)
    }

    private def renderTitle(p: Props): VdomElement = {
      <.div(
        ^.marginBottom := "8px",
        GridContainer(spacing = 0,
                      justify = Grid.Justify.SpaceBetween,
                      alignItems = Grid.AlignItems.Center)(
          GridItem()(Typography(variant = Typography.Variants.Title)(p.title)),
          Hidden(xsUp = p.moreRoute.isEmpty)(
            GridItem()(FlatRoundedButton(
              onClick = p.moreRoute.map(doteRouterCtl.set).getOrElse(Callback.empty))("See More"))
          )
        )
      )
    }

    private def renderPage(p: Props,
                           allItems: Seq[raw.React.Node],
                           tilesPerPage: Int,
                           tilesPerRow: Int,
                           selectedPageIndex: Int,
                           pageIndex: Int): VdomNode = {
      val widthPercent = 100.0 / tilesPerRow

      // Don't render the actual items if this page isn't visible. This is a hack to
      // speed up the render time of the page.
      val items = if (selectedPageIndex == pageIndex) allItems else Nil

      val pageItems: Seq[raw.React.Node] = items
        .drop(pageIndex * tilesPerPage)
        // Pad with placeholders to make the list spacing balanced, rendering code below
        // will handle the placeholders
        .padTo(tilesPerPage, <.div(^.width := s"$widthPercent%", ^.height := "100px").rawNode)
        // take the number that fit
        .take(tilesPerPage)

      GridContainer(key = Some(s"${p.title}-page-$pageIndex"),
                    style = Styles.itemsContainer,
                    spacing = 16,
                    alignItems = Grid.AlignItems.FlexStart,
                    justify = Grid.Justify.FlexStart)(
        pageItems.zipWithIndex map {
          case (item, i) =>
            GridItem(key = Some(s"page-$pageIndex-item-$i"),
                     style = js.Dynamic.literal("width" -> s"$widthPercent%"))(item)
        } toVdomArray
      )
    }

    def render(p: Props, pc: PropsChildren, s: State): VdomElement = {
      val numTilesPerRow: Int = calcItemsPerRow(p, s)

      val maxRows =
        if (numTilesPerRow == 1) {
          3
        } else if (numTilesPerRow < 6) {
          2
        } else {
          1
        }

      val items = pc.toList

      val numRows =
        if (p.padEmptyRows) {
          maxRows
        } else {
          Math.min(Math.ceil(items.size.toDouble / numTilesPerRow).toInt, maxRows)
        }

      val numTilesPerPage = numTilesPerRow * numRows
      val partialPage = if (items.size % numTilesPerPage > 0) 1 else 0
      val numPages = (items.size / numTilesPerPage) + partialPage

      <.div(
        renderTitle(p),
        CompactListPagination(pageIndex = s.pageIndex,
                              onIndexChanged = (index) => bs.modState(_.copy(pageIndex = index)))(
          (0 until numPages) map { i =>
            renderPage(p, items, numTilesPerPage, numTilesPerRow, s.pageIndex, i)
          } toVdomArray
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => State(breakpoint = currentBreakpointString, pageIndex = 0))
    .backend(new Backend(_))
    .renderPCS((builder, props, pc, state) => builder.backend.render(props, pc, state))
    .componentWillUnmount(x => x.backend.handleWillUnmount)
    .build

  def apply(itemsPerRowBreakpoints: Map[String, Int] = Map.empty,
            title: String = "",
            moreRoute: Option[DoteRoute] = None,
            padEmptyRows: Boolean = true) = {
    component.withProps(Props(itemsPerRowBreakpoints, title, moreRoute, padEmptyRows))
  }

}
