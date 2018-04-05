package kurtome.dote.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.Debounce
import org.scalajs.dom
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

import scala.scalajs.js

object FullItemList extends LogSupport {

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
                   caption: String,
                   pageIndex: Int = 0,
                   prevPageRoute: Option[DoteRoute],
                   nextPageRoute: Option[DoteRoute])
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

    private def renderPagination(p: Props): VdomElement = {
      if (p.prevPageRoute.isDefined || p.nextPageRoute.isDefined)
        GridContainer(alignItems = Grid.AlignItems.Center)(
          GridItem()(if (p.prevPageRoute.isDefined) {
            SiteLink(p.prevPageRoute.get)(Icons.ChevronLeft())
          } else {
            <.span()
          }),
          GridItem()(
            Typography(component = "span", variant = Typography.Variants.Body1)(
              s"Page ${p.pageIndex + 1}")),
          GridItem()(if (p.nextPageRoute.isDefined) {
            SiteLink(p.nextPageRoute.get)(Icons.ChevronRight())
          } else {
            <.span()
          })
        )
      else {
        <.div()
      }
    }

    private def renderTitle(p: Props): VdomElement = {
      <.div(
        ^.marginBottom := "8px",
        GridContainer(spacing = 0,
                      justify = Grid.Justify.SpaceBetween,
                      alignItems = Grid.AlignItems.Center)(
          GridItem()(
            <.div(
              Typography(variant = Typography.Variants.Title)(p.title),
              Typography(variant = Typography.Variants.Caption)(p.caption)
            )),
          GridItem()(renderPagination(p))
        )
      )
    }

    private def renderPage(p: Props, allItems: Seq[raw.ReactNode], tilesPerRow: Int): VdomNode = {
      val widthPercent = 100.0 / tilesPerRow

      GridContainer(style = Styles.itemsContainer,
                    spacing = 16,
                    alignItems = Grid.AlignItems.FlexStart,
                    justify = Grid.Justify.SpaceBetween)(
        allItems.zipWithIndex map {
          case (item, i) =>
            GridItem(key = Some(s"item-$i"),
                     style = js.Dynamic.literal("width" -> s"$widthPercent%"))(item)
        } toVdomArray
      )
    }

    def render(p: Props, pc: PropsChildren, s: State): VdomElement = {
      val numTilesPerRow: Int = calcItemsPerRow(p, s)

      val numRows =
        if (numTilesPerRow == 1) {
          3
        } else if (numTilesPerRow < 6) {
          2
        } else {
          1
        }

      val items = pc.toList

      <.div(
        renderTitle(p),
        CompactListPagination(pageIndex = s.pageIndex,
                              onIndexChanged = (index) => bs.modState(_.copy(pageIndex = index)))(
          renderPage(p, items, numTilesPerRow)
        ),
        GridContainer(justify = Grid.Justify.Center)(GridItem()(renderPagination(p)))
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
            caption: String = "",
            pageIndex: Int = 0,
            prevPageRoute: Option[DoteRoute] = None,
            nextPageRoute: Option[DoteRoute] = None) = {
    component.withProps(
      Props(itemsPerRowBreakpoints, title, caption, pageIndex, prevPageRoute, nextPageRoute))
  }

}
