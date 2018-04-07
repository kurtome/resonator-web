package kurtome.dote.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.get_feed._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.ActivityId
import kurtome.dote.proto.api.feed.FeedId.FollowerSummaryId
import kurtome.dote.proto.api.feed.FeedId.TagListId
import kurtome.dote.proto.api.feed._
import kurtome.dote.shared.constants.QueryParamKeys
import kurtome.dote.shared.mapper.TagMapper
import kurtome.dote.shared.model.Tag
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.AllActivityRoute
import kurtome.dote.web.DoteRoutes.FollowersRoute
import kurtome.dote.web.DoteRoutes.FollowingActivityRoute
import kurtome.dote.web.DoteRoutes.ProfileActivityRoute
import kurtome.dote.web.DoteRoutes.TagRoute
import kurtome.dote.web.components.widgets.feed.VerticalFeed
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.FeedIdRoutes.TagKindUrlMapper
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object FeedView extends LogSupport {

  val defaultPageSize = 24

  object Styles extends StyleSheet.Inline {
    import dsl._

  }

  case class Props(feedId: FeedId)
  case class State(feed: Feed = Feed.defaultInstance, isFeedLoading: Boolean = true)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleDidUpdate = (p: Props) => Callback { fetchFeed(p) }

    def fetchFeed(p: Props) = {
      bs.modState(_.copy(isFeedLoading = true)).runNow()

      // get the latest data as well, in case it has changed
      val f = DoteProtoServer.getFeed(
        GetFeedRequest(maxItems = 10, maxItemSize = 12, id = Some(p.feedId))) map { response =>
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
    .componentDidMount(x => x.backend.handleDidUpdate(x.props))
    .componentWillReceiveProps(x => x.backend.handleDidUpdate(x.nextProps))
    .build

  private def extractPaginationInfo(queryParams: Map[String, String]) = {
    val pageIndex =
      Try(queryParams.getOrElse(QueryParamKeys.pageIndex, "0").toInt).getOrElse(0)
    val paginationInfo = PaginationInfo(pageIndex, defaultPageSize)
    paginationInfo
  }

  def apply(route: TagRoute) = {
    val dotableKind = if (route.queryParams.get(QueryParamKeys.dotableKind).contains("episode")) {
      Dotable.Kind.PODCAST_EPISODE
    } else {
      Dotable.Kind.PODCAST
    }

    val paginationInfo: PaginationInfo = extractPaginationInfo(route.queryParams)
    val tag = TagMapper.toProto(Tag(TagKindUrlMapper.fromUrl(route.kind), route.key, route.key))
    val id = FeedId().withTagList(
      TagListId()
        .withTag(tag)
        .withDotableKind(dotableKind)
        .withPaginationInfo(paginationInfo))
    component.withProps(Props(id))
  }

  def apply(followersRoute: FollowersRoute) = {
    val id =
      FeedId().withFollowerSummary(FollowerSummaryId().withUsername(followersRoute.username))
    component.withProps(Props(id))
  }

  def apply(route: AllActivityRoute) = {
    val paginationInfo: PaginationInfo = extractPaginationInfo(route.queryParams)
    val id = FeedId().withActivity(ActivityId().withPaginationInfo(paginationInfo))
    component.withProps(Props(id))
  }

  def apply(route: FollowingActivityRoute) = {
    val paginationInfo: PaginationInfo = extractPaginationInfo(route.queryParams)
    val id =
      FeedId().withActivity(
        ActivityId().withPaginationInfo(paginationInfo).withFollowingOnly(true))
    component.withProps(Props(id))
  }

  def apply(route: ProfileActivityRoute) = {
    val paginationInfo: PaginationInfo = extractPaginationInfo(route.queryParams)
    val id =
      FeedId().withActivity(
        ActivityId().withPaginationInfo(paginationInfo).withUsername(route.username))
    component.withProps(Props(id))
  }
}
