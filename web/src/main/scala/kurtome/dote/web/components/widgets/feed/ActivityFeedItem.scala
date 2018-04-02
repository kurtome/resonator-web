package kurtome.dote.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.activity.Activity
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.lib.SwipeableViews
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.components.widgets.button.PageArrowButton
import kurtome.dote.web.components.widgets.card.ActivityCard
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.Debounce
import kurtome.dote.web.utils.FeedIdRoutes
import org.scalajs.dom
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

import scala.language.postfixOps
import scala.scalajs.js

object ActivityFeedItem extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val title = style(
      marginBottom(SharedStyles.spacingUnit)
    )

    val tileContainer = style(
      marginTop(0 px)
    )

    val accountIcon = style(
      float.left
    )

    val ratingText = style(
      float.right
    )

    val username = style(
      marginLeft(1.9 em), // left space for account icon
      lineHeight(1.75 em)
    )

    val swipeRoot = style(
      position.relative,
      width :=! "caclc(100% + 16px)",
      marginLeft(-8 px),
      marginRight(-8 px)
    )

    val itemsContainer = style(
      width(100 %%),
      marginLeft(0 px),
      marginRight(0 px),
      marginBottom(8 px)
    )

    val pageButtonGridContainer = style(
      height(100 %%)
    )
  }
  Styles.addToDocument()

  case class Props(feedItem: FeedItem)
  case class State(availableWidthPx: Int, pageIndex: Int = 0)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val updateTileSize: Callback = {
      val p = bs.props.runNow()
      bs.modState(
        _.copy(availableWidthPx = ContentFrame.innerWidthPx,
               // reset to first page since number of pages may have changed
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
      val list = p.feedItem.getActivityList.getActivityList
      val title = list.title
      val caption = list.caption

      val titleRoute = FeedIdRoutes.toRoute(p.feedItem.getId)
      Typography(variant = Typography.Variants.Title, style = Styles.title)(title)
    }

    private def renderPage(activities: Seq[Activity],
                           numTilesPerPage: Int,
                           tileWidth: Int,
                           pageIndex: Int): VdomNode = {
      val visibleActivities = activities
        .drop(numTilesPerPage * pageIndex)
        // Pad with placeholders to make the list spacing balanced, rendering code below
        // will handle the placeholders
        .padTo(numTilesPerPage, Activity.defaultInstance)
        // take the number that fit
        .take(numTilesPerPage)

      GridContainer(key = Some("page" + pageIndex),
                    spacing = 16,
                    justify = Grid.Justify.SpaceBetween,
                    style = Styles.itemsContainer)(
        visibleActivities.zipWithIndex map {
          case (activity, i) =>
            if (activity.content.isEmpty) {
              GridItem(key = Some("empty" + i))()
            } else {
              val dotable = activity.getDote.getDotable
              GridItem(key = Some(dotable.id + i), style = Styles.tileContainer)(
                HoverPaper(variant = HoverPaper.Variants.CardHeader)(
                  ActivityCard(activity, tileWidth)()
                )
              )
            }
        } toVdomArray
      )
    }

    def render(p: Props, s: State): VdomElement = {
      val list = p.feedItem.getActivityList.getActivityList.items
      val activities = list

      val numTilesPerRow: Int = currentBreakpointString match {
        case "xs" => 1
        case "sm" => 2
        case "md" => 2
        case _ => 3
      }
      val numRows = if (isBreakpointXs) 3 else 2

      val numTiles = numRows * numTilesPerRow
      val tileWidth = (s.availableWidthPx - (numTilesPerRow * 16)) / numTilesPerRow

      val partialPage = if (activities.size % numTiles > 0) 1 else 0
      val numPages = (activities.size / numTiles) + partialPage

      val canPrevPage = s.pageIndex != 0 && numPages > 0
      val canNextPage = s.pageIndex != (numPages - 1)

      MainContentSection(variant = MainContentSection.chooseVariant(p.feedItem.getCommon))(
        renderTitle(p),
        MuiThemeProvider(MuiTheme.lightTheme)(
          <.div(
            ^.position := "relative",
            Hidden(xsDown = true, smUp = !(canPrevPage || canNextPage))(
              <.div(
                ^.position := "absolute",
                ^.marginLeft := "-64px",
                ^.height := "100%",
                ^.width := "calc(100% + 128px)",
                GridContainer(style = Styles.pageButtonGridContainer,
                              justify = Grid.Justify.SpaceBetween,
                              alignItems = Grid.AlignItems.Center,
                              spacing = 0)(
                  GridItem()(
                    Fader(in = canPrevPage)(
                      PageArrowButton(
                        direction = PageArrowButton.Directions.Previous,
                        disabled = !canPrevPage,
                        onClick = bs.modState(state =>
                          state.copy(pageIndex = Math.max(0, state.pageIndex - 1))))()
                    )),
                  GridItem()(
                    Fader(in = canNextPage)(
                      PageArrowButton(
                        direction = PageArrowButton.Directions.Next,
                        disabled = !canNextPage,
                        onClick = bs.modState(state =>
                          state.copy(pageIndex = Math.min(state.pageIndex + 1, numPages)))
                      )(Icons.ChevronRight())
                    )
                  )
                )
              )
            ),
            <.div(
              ^.className := Styles.swipeRoot,
              SwipeableViews(index = s.pageIndex,
                             onIndexChanged = (index: Int, indexLatest: Int, meta: js.Dynamic) =>
                               bs.modState(_.copy(pageIndex = index)))(
                (0 until numPages) map { pageIndex =>
                  renderPage(activities, numTiles, tileWidth, pageIndex)
                } toVdomArray
              )
            )
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => State(availableWidthPx = ContentFrame.innerWidthPx))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .componentWillUnmount(x => x.backend.handleWillUnmount)
    .build

  def apply(feedItem: FeedItem, key: Option[String] = None) = {
    assert(feedItem.content.isActivityList)
    if (key.isDefined) {
      component.withKey(key.get).withProps(Props(feedItem))
    } else {
      component.withProps(Props(feedItem))
    }
  }

}
