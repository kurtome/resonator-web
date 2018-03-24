package kurtome.dote.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.activity.Activity
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.shared.constants.Emojis
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.ProfileRoute
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.Debounce
import kurtome.dote.web.utils.FeedIdRoutes
import org.scalajs.dom
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

import scala.scalajs.js

object ActivityFeedItem extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val title = style(
      marginBottom(SharedStyles.spacingUnit)
    )

    val paperWrapper = style(
      padding(SharedStyles.spacingUnit)
    )

    val tileContainer = style(
      marginTop(0 px)
    )

    val accountIcon = style(
      float.left
    )

    val username = style(
      marginLeft(1.9 em), // left space for account icon
      lineHeight(1.7 em)
    )
  }
  Styles.addToDocument()

  case class Props(feedItem: FeedItem)
  case class State(availableWidthPx: Int)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val updateTileSize: Callback = {
      val p = bs.props.runNow()
      bs.modState(_.copy(availableWidthPx = ContentFrame.innerWidthPx))
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

      val padding = 16

      val numTiles = numRows * numTilesPerRow
      val tileWidth = (s.availableWidthPx - (numTilesPerRow * 16)) / numTilesPerRow

      val visibleActivities = activities
      // Pad with placeholders to make the list spacing balanced, rendering code below
      // will handle the placeholders
        .padTo(numTiles, Activity.defaultInstance)
        // take the number that fit
        .take(numTiles)

      GridContainer(spacing = 0)(
        GridItem(xs = 12)(
          renderTitle(p),
          GridContainer(spacing = 16, justify = Grid.Justify.SpaceBetween)(
            visibleActivities.zipWithIndex map {
              case (activity, i) =>
                val dotable = activity.getDote.getDotable
                val dote = activity.getDote.getDote
                val smile = Emojis.smileEmojis.lift(dote.smileCount - 1).getOrElse("")
                val cry = Emojis.cryEmojis.lift(dote.cryCount - 1).getOrElse("")
                val laugh = Emojis.laughEmojis.lift(dote.laughCount - 1).getOrElse("")
                val scowl = Emojis.scowlEmojis.lift(dote.scowlCount - 1).getOrElse("")
                val isLastInRow = ((i + 1) % numTilesPerRow) == 0
                GridItem(key = Some(dotable.id + i), style = Styles.tileContainer)(
                  <.div(
                    ^.width := asPxStr(tileWidth),
                    Paper(style = Styles.paperWrapper, elevation = 1)(
                      <.div(
                        ^.width := "100%",
                        ^.display := "inline-block",
                        Typography(style = Styles.accountIcon)(Icons.AccountCircle()),
                        Typography(noWrap = true, style = Styles.username)(
                          SiteLink(ProfileRoute(dote.getPerson.username))(dote.getPerson.username))
                      ),
                      Typography(noWrap = true)(s"Rated $smile$cry$laugh$scowl"),
                      if (dotable.kind == Dotable.Kind.PODCAST) {
                        PodcastTile(dotable = dotable,
                                    width = "100px",
                                    elevation = 3,
                                    disableActions = true)()
                      } else if (dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
                        EpisodeTile(dotable = dotable,
                                    width = tileWidth - padding,
                                    elevation = 3,
                                    disableActions = true)()
                      } else {
                        // Placeholder for correct spacing
                        <.div(^.width := asPxStr(tileWidth))
                      }
                    )
                  )
                )
            } toVdomArray
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
