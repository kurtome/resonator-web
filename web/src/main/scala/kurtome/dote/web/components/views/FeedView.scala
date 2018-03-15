package kurtome.dote.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.get_feed._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.FollowerSummaryId
import kurtome.dote.proto.api.feed.FeedId.TagListId
import kurtome.dote.proto.api.feed._
import kurtome.dote.shared.mapper.TagMapper
import kurtome.dote.shared.model.Tag
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.FollowersRoute
import kurtome.dote.web.DoteRoutes.TagRoute
import kurtome.dote.web.DoteRoutes.TagRoute
import kurtome.dote.web.components.widgets.feed.VerticalFeed
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.FeedIdRoutes.TagKindUrlMapper
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object FeedView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val announcementWrapper = style(
      marginTop(24 px),
      marginBottom(24 px)
    )
  }

  case class Props(feedId: FeedId)
  case class State(feed: Feed = Feed.defaultInstance, isFeedLoading: Boolean = true)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleDidMount = (p: Props) => Callback { fetchFeed(p) }

    def fetchFeed(p: Props) = {
      // get the latest data as well, in case it has changed
      val f = DoteProtoServer.getFeed(
        GetFeedRequest(maxItems = 10, maxItemSize = 30, id = Some(p.feedId))) map { response =>
        bs.modState(_.copy(feed = response.getFeed, isFeedLoading = false)).runNow()
      }
      GlobalLoadingManager.addLoadingFuture(f)
    }

    def render(p: Props, s: State): VdomElement = {
      VerticalFeed(s.feed, s.isFeedLoading)()
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .componentDidMount(x => x.backend.handleDidMount(x.props))
    .build

  def apply(route: TagRoute) = {
    val dotableKind = if (route.queryParams.get("dk").contains("episode")) {
      Dotable.Kind.PODCAST_EPISODE
    } else {
      Dotable.Kind.PODCAST
    }
    val tag = TagMapper.toProto(Tag(TagKindUrlMapper.fromUrl(route.kind), route.key, route.key))
    val id = FeedId().withTagList(TagListId().withTag(tag).withDotableKind(dotableKind))
    component.withProps(Props(id))
  }

  def apply(followersRoute: FollowersRoute) = {
    val id =
      FeedId().withFollowerSummary(FollowerSummaryId().withUsername(followersRoute.username))
    component.withProps(Props(id))
  }
}
