package resonator.web.components.widgets.feed

import resonator.proto.api.dotable.Dotable
import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.VdomElement
import resonator.proto.api.feed.FeedDotableList
import resonator.proto.api.feed.FeedItem
import resonator.web.CssSettings._
import resonator.web.components.ComponentHelpers._
import resonator.web.components.widgets._
import resonator.web.components.widgets.card.EpisodeCard
import resonator.web.components.widgets.card.PodcastCard
import resonator.web.utils.BaseBackend
import resonator.web.utils.FeedIdRoutes
import wvlet.log.LogSupport

import scala.scalajs.js
import scalacss.ScalaCssReact._

object DotableListFeedItem extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

  }
  Styles.addToDocument()

  case class Props(feedItem: FeedItem)
  case class State()

  private def calcTilesPerRowMap(p: Props, s: State): Map[String, Int] = {
    p.feedItem.getDotableList.getList.dotables.headOption.map(_.kind) match {
      case Some(Dotable.Kind.PODCAST_EPISODE) =>
        Map(
          "xs" -> 1,
          "sm" -> 2,
          "md" -> 2,
          "lg" -> 3,
          "xl" -> 3
        )
      case _ =>
        Map(
          "xs" -> 2,
          "sm" -> 3,
          "md" -> 4,
          "lg" -> 5,
          "xl" -> 5
        )
    }
  }

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def render(p: Props, s: State): VdomElement = {
      val list = p.feedItem.getDotableList.getList
      val dotables = list.dotables

      MainContentSection(variant = MainContentSection.chooseVariant(p.feedItem.getCommon))(
        p.feedItem.getDotableList.style match {
          case FeedDotableList.Style.PRIMARY => {
            FullItemList(
              title = p.feedItem.getDotableList.getList.title,
              caption = p.feedItem.getDotableList.getList.caption,
              pageIndex = FeedIdRoutes.pageIndex(p.feedItem.getId),
              prevPageRoute = FeedIdRoutes.prevPageRoute(p.feedItem.getId),
              nextPageRoute =
                if (dotables.size < p.feedItem.getId.getTagList.getPaginationInfo.pageSize) None
                else FeedIdRoutes.nextPageRoute(p.feedItem.getId),
              itemsPerRowBreakpoints = calcTilesPerRowMap(p, s)
            )(renderItems(dotables))
          }
          case _ =>
            CompactItemList(
              title = p.feedItem.getDotableList.getList.title,
              moreRoute = FeedIdRoutes.toRoute(p.feedItem.getId),
              itemsPerRowBreakpoints = calcTilesPerRowMap(p, s)
            )(renderItems(dotables))
        }
      )
    }

    private def renderItems(dotables: Seq[Dotable]) = {
      dotables.zipWithIndex map {
        case (dotable, i) =>
          val key = s"${dotable.id}-$i"
          if (dotable.kind == Dotable.Kind.PODCAST) {
            DotableActionsCardWrapper(dotable).withKey(key)(
              PodcastCard(dotable = dotable, variant = PodcastCard.Variants.ImageOnly)())
          } else {
            DotableActionsCardWrapper(dotable).withKey(key)(EpisodeCard(dotable = dotable)())
          }
      } toVdomArray
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
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
