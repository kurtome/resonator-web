package resonator.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.feed.FeedId.FollowerSummaryId
import resonator.proto.api.feed.FeedId.Id
import resonator.proto.api.feed.FeedId.TagCollectionId
import resonator.web.CssSettings._
import resonator.web.components.ComponentHelpers._
import resonator.proto.api.feed._
import resonator.web.components.lib.LazyLoad
import resonator.web.components.materialui.CircularProgress
import resonator.web.components.materialui.Grid
import resonator.web.components.materialui.GridContainer
import resonator.web.components.materialui.GridItem
import resonator.web.utils.BaseBackend
import wvlet.log.LogSupport

object VerticalFeed extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._
  }

  case class Props(feed: Feed, isLoading: Boolean)

  class Backend(val bs: BackendScope[Props, Unit]) extends BaseBackend(Styles) {

    def render(p: Props): VdomElement = {
      if (p.isLoading) {
        GridContainer(spacing = 24, justify = Grid.Justify.Center)(
          GridItem()(
            <.div(
              ^.margin := "64px",
              CircularProgress(variant = CircularProgress.Variant.Indeterminate, size = 60f)()
            )
          )
        )
      } else {
        <.div(
          p.feed.items.zipWithIndex map {
            case (item, i) =>
              <.div(
                ^.key := s"$i${item.getDotableList.getList.title}",
                item.getId.id match {
                  case Id.DotableList(_) =>
                    LazyLoad(once = true,
                             height = 150,
                             key = Some(s"$i${item.getDotableList.getList.title}"))(
                      DotableListFeedItem(item)()
                    )
                  case Id.TagList(_) =>
                    LazyLoad(once = true,
                             height = 150,
                             key = Some(s"$i${item.getDotableList.getList.title}"))(
                      DotableListFeedItem(item)()
                    )
                  case Id.ProfileDoteList(_) =>
                    LazyLoad(once = true,
                             height = 150,
                             key = Some(s"$i${item.getDotableList.getList.title}"))(
                      DotableListFeedItem(item)()
                    )
                  case Id.FollowerSummary(FollowerSummaryId(username)) =>
                    LazyLoad(once = true,
                             height = 100,
                             key = Some(s"$i-profile-summary-$username"))(
                      FollowerSummaryFeedItem(item)()
                    )
                  case Id.TagCollection(TagCollectionId()) =>
                    LazyLoad(once = true, height = 100, key = Some(s"$i-tag-collection"))(
                      TagCollectionFeedItem(item)()
                    )
                  case Id.Activity(_) =>
                    LazyLoad(once = true, height = 100, key = Some(s"$i-tag-collection"))(
                      ActivityFeedItem(item)()
                    )
                  case _ => {
                    warn(s"unexpected kind ${item.getId.id}")
                    <.div(^.key := i)
                  }
                }
              )
          } toVdomArray
        )
      }
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderP((builder, props) => builder.backend.render(props))
    .build

  def apply(feed: Feed, isLoading: Boolean) = component.withProps(Props(feed, isLoading))
}
