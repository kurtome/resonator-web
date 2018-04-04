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
  case class State(breakpoint: String, pageIndex: Int)

  private def calcTilesPerRow(p: Props, s: State): Int = {
    p.feedItem.getDotableList.getList.dotables.headOption.map(_.kind) match {
      case Some(Dotable.Kind.PODCAST_EPISODE) => {
        s.breakpoint match {
          case "xs" => 1
          case "sm" => 2
          case "md" => 2
          case _ => 3
        }
      }
      case _ =>
        s.breakpoint match {
          case "xs" => 2
          case "sm" => 3
          case "md" => 4
          case _ => 5
        }
    }
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
      val list = p.feedItem.getDotableList.getList
      val title = list.title
      val caption = list.caption

      val titleRoute = FeedIdRoutes.toRoute(p.feedItem.getId)
      p.feedItem.getDotableList.style match {
        case FeedDotableList.Style.PRIMARY => {
          <.div(
            ^.marginBottom := "8px",
            Typography(variant = Typography.Variants.Headline)(title),
            Typography(variant = Typography.Variants.Caption)(caption)
          )
        }
        case _ => {
          <.div(
            ^.marginBottom := "8px",
            GridContainer(spacing = 0,
                          justify = Grid.Justify.SpaceBetween,
                          alignItems = Grid.AlignItems.Center)(
              GridItem()(Typography(variant = Typography.Variants.Title)(title)),
              GridItem(hidden = Grid.HiddenProps(xsUp = titleRoute.isEmpty))(FlatRoundedButton(
                onClick = titleRoute.map(doteRouterCtl.set).getOrElse(Callback.empty))("See More"))
            )
          )
        }
      }
    }

    private def renderPage(allDotables: Seq[Dotable],
                           tilesPerPage: Int,
                           tilesPerRow: Int,
                           pageIndex: Int): VdomNode = {
      val dotables = allDotables
        .drop(pageIndex * tilesPerPage)
        // Pad with placeholders to make the list spacing balanced, rendering code below
        // will handle the placeholders
        .padTo(tilesPerPage, Dotable.defaultInstance)
        // take the number that fit
        .take(tilesPerPage)

      val widthPercent = 100.0 / tilesPerRow

      GridContainer(style = Styles.itemsContainer,
                    spacing = 16,
                    alignItems = Grid.AlignItems.FlexStart,
                    justify = Grid.Justify.SpaceBetween)(
        dotables.zipWithIndex map {
          case (dotable, i) =>
            GridItem(key = Some(dotable.id + i),
                     style = js.Dynamic.literal("width" -> s"$widthPercent%"))(
              if (dotable.kind == Dotable.Kind.PODCAST) {
                HoverPaper()(PodcastCard(dotable = dotable)())
              } else if (dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
                HoverPaper()(EpisodeCard(dotable = dotable)())
              } else {
                // Placeholder for correct spacing
                <.div(^.width := s"$widthPercent%", ^.height := "100px")
              }
            )
        } toVdomArray
      )
    }

    def render(p: Props, s: State): VdomElement = {
      val list = p.feedItem.getDotableList.getList

      val numTilesPerRow: Int = calcTilesPerRow(p, s)

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

      val numTilesPerPage = numTilesPerRow * numRows
      val partialPage = if (dotables.size % numTilesPerPage > 0) 1 else 0
      val numPages = (dotables.size / numTilesPerPage) + partialPage

      MainContentSection(variant = MainContentSection.chooseVariant(p.feedItem.getCommon))(
        renderTitle(p),
        CompactListPagination(pageIndex = s.pageIndex,
                              onIndexChanged = (index) => bs.modState(_.copy(pageIndex = index)))(
          (0 until numPages) map { i =>
            renderPage(dotables, numTilesPerPage, numTilesPerRow, s.pageIndex)
          } toVdomArray
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => State(breakpoint = currentBreakpointString, pageIndex = 0))
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
