package kurtome.dote.web.components.widgets.feed

import kurtome.dote.proto.api.dotable.Dotable
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.VdomElement
import kurtome.dote.proto.api.feed.FeedDotableList
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.components.widgets.card.EpisodeCard
import kurtome.dote.web.components.widgets.card.PodcastCard
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.Debounce
import kurtome.dote.web.utils.FeedIdRoutes
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js
import scalacss.ScalaCssReact._

object DotableListFeedItem extends LogSupport {

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

  case class Props(feedItem: FeedItem)
  case class State(tileSizePx: Int, availableWidthPx: Int, pageIndex: Int)

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
    p.feedItem.getDotableList.getList.dotables.headOption.map(_.kind) match {
      case Some(Dotable.Kind.PODCAST_EPISODE) => episodeTileSizes(currentBreakpointString)
      case _ => breakpointTileSizes(currentBreakpointString)
    }
  }

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val updateTileSize: Callback = {
      val p = bs.props.runNow()
      bs.modState(
        _.copy(tileSizePx = currentTileSizePx(p),
               availableWidthPx = ContentFrame.innerWidthPx,
               pageIndex = 0))
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
      val list = p.feedItem.getDotableList.getList
      val title = list.title
      val caption = list.caption

      val titleRoute = FeedIdRoutes.toRoute(p.feedItem.getId)
      p.feedItem.getDotableList.style match {
        case FeedDotableList.Style.PRIMARY => {
          <.div(
            Typography(variant = Typography.Variants.Headline)(title),
            Typography(variant = Typography.Variants.Caption)(caption)
          )
        }
        case _ => {
          GridContainer(spacing = 0,
                        justify = Grid.Justify.SpaceBetween,
                        alignItems = Grid.AlignItems.Center)(
            GridItem()(Typography(variant = Typography.Variants.Title)(title)),
            GridItem(hidden = Grid.HiddenProps(xsUp = titleRoute.isEmpty))(FlatRoundedButton(
              onClick = titleRoute.map(doteRouterCtl.set).getOrElse(Callback.empty))("See More"))
          )
        }
      }
    }

    private def renderPage(allDotables: Seq[Dotable],
                           tilesPerPage: Int,
                           tileWidthPx: Int,
                           pageIndex: Int): VdomNode = {
      val dotables = allDotables
        .drop(pageIndex * tilesPerPage)
        // Pad with placeholders to make the list spacing balanced, rendering code below
        // will handle the placeholders
        .padTo(tilesPerPage, Dotable.defaultInstance)
        // take the number that fit
        .take(tilesPerPage)

      Grid(container = true,
           style = Styles.itemsContainer,
           spacing = 16,
           alignItems = Grid.AlignItems.FlexStart,
           justify = Grid.Justify.SpaceBetween)(
        dotables.zipWithIndex map {
          case (dotable, i) =>
            Grid(key = Some(dotable.id + i), item = true, style = Styles.tileContainer)(
              if (dotable.kind == Dotable.Kind.PODCAST) {
                HoverPaper()(PodcastCard(dotable = dotable, width = asPxStr(tileWidthPx))())
              } else if (dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
                HoverPaper()(EpisodeCard(dotable = dotable, width = tileWidthPx)())
              } else {
                // Placeholder for correct spacing
                <.div(^.width := asPxStr(tileWidthPx), ^.height := "100px")
              }
            )
        } toVdomArray
      )
    }

    def render(p: Props, s: State): VdomElement = {
      val requestedWidth = s.tileSizePx
      val list = p.feedItem.getDotableList.getList
      // the actual padding will be dynamic since the tiles are evenly spaced, so just make sure
      // the number of tiles has some extra space
      val tilePaddingPx = requestedWidth / 5
      // this calculation assumes all the tiles will be the same width (i.e. the same type of
      // dotable)
      val numTilesPerRow: Int = s.availableWidthPx / (requestedWidth + tilePaddingPx)
      val numRows =
        p.feedItem.getDotableList.style match {
          case FeedDotableList.Style.PRIMARY => {
            if (list.dotables.length % numTilesPerRow == 0) {
              list.dotables.length / numTilesPerRow
            } else {
              (list.dotables.length / numTilesPerRow) + 1
            }
          }
          case _ => {
            if (numTilesPerRow == 1) {
              3
            } else if (numTilesPerRow < 6) {
              2
            } else {
              1
            }
          }
        }

      val dotables = list.dotables

      val minPaddingPx = 12
      val leftoverWidthPx = s.availableWidthPx - (numTilesPerRow * (requestedWidth + minPaddingPx))
      val tileWidthPx = if (numTilesPerRow == 1) {
        // use the entire width if there is only one per row
        s.availableWidthPx
      } else {
        requestedWidth + (leftoverWidthPx / numTilesPerRow)
      }

      val numTilesPerPage = numTilesPerRow * numRows
      val partialPage = if (dotables.size % numTilesPerPage > 0) 1 else 0
      val numPages = (dotables.size / numTilesPerPage) + partialPage

      MainContentSection(variant = MainContentSection.chooseVariant(p.feedItem.getCommon))(
        renderTitle(p),
        CompactListPagination(pageIndex = s.pageIndex,
                              onIndexChanged = (index) => bs.modState(_.copy(pageIndex = index)))(
          (0 until numPages) map { i =>
            renderPage(dotables, numTilesPerPage, tileWidthPx, s.pageIndex)
          } toVdomArray
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(
      p =>
        State(tileSizePx = currentTileSizePx(p),
              availableWidthPx = ContentFrame.innerWidthPx,
              pageIndex = 0))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .componentWillUnmount(x => x.backend.handleWillUnmount)
    .build

  def apply(feedItem: FeedItem, key: Option[String] = None) = {
    assert(feedItem.content.isDotableList)
    if (key.isDefined) {
      component.withKey(key.get).withProps(Props(feedItem))
    } else {
      component.withProps(Props(feedItem))
    }
  }

}
