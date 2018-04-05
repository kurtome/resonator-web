package kurtome.dote.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
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

    val title = style(
      marginBottom(SharedStyles.spacingUnit)
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
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

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
        CompactItemList(title = p.feedItem.getActivityList.getActivityList.title,
                        moreRoute = FeedIdRoutes.toRoute(p.feedItem.getId),
                        itemsPerRowBreakpoints = tilesPerRow)(
          activities map { activity =>
            HoverPaper(variant = HoverPaper.Variants.CardHeader)(ActivityCard(activity)())
          } toVdomArray
        )
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
