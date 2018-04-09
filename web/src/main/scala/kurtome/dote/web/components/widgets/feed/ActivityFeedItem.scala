package kurtome.dote.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.activity.Activity
import kurtome.dote.proto.api.feed.FeedActivityList
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.components.widgets.card.ActivityCard
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.FeedIdRoutes
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

import scala.language.postfixOps

object ActivityFeedItem extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

  }
  Styles.addToDocument()

  case class Props(feedItem: FeedItem)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    private def renderItems(activities: Seq[Activity]) = {
      activities.zipWithIndex map {
        case (activity, i) =>
          val key = s"${activity.getDote.getDotable.id}-$i"
          HoverPaper(variant = HoverPaper.Variants.CardHeader)
            .withKey(key)(ActivityCard(activity)())
      } toVdomArray
    }

    def render(p: Props, s: State): VdomElement = {
      val activities = p.feedItem.getActivityList.getActivityList.items

      val tilesPerRow = Map(
        "xs" -> 1,
        "sm" -> 2,
        "md" -> 2,
        "lg" -> 3,
        "xl" -> 3
      )

      MainContentSection(variant = MainContentSection.chooseVariant(p.feedItem.getCommon))(
        p.feedItem.getActivityList.style match {
          case FeedActivityList.Style.PRIMARY => {
            FullItemList(
              title = p.feedItem.getActivityList.getActivityList.title,
              caption = p.feedItem.getActivityList.getActivityList.caption,
              pageIndex = FeedIdRoutes.pageIndex(p.feedItem.getId),
              prevPageRoute = FeedIdRoutes.prevPageRoute(p.feedItem.getId),
              nextPageRoute =
                if (activities.size < p.feedItem.getId.getActivity.getPaginationInfo.pageSize) None
                else FeedIdRoutes.nextPageRoute(p.feedItem.getId),
              itemsPerRowBreakpoints = tilesPerRow
            )(renderItems(activities))
          }
          case _ =>
            CompactItemList(title = p.feedItem.getActivityList.getActivityList.title,
                            moreRoute = FeedIdRoutes.toRoute(p.feedItem.getId),
                            itemsPerRowBreakpoints = tilesPerRow)(renderItems(activities))
        }
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
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
