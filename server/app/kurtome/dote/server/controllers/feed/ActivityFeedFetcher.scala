package kurtome.dote.server.controllers.feed

import javax.inject._
import kurtome.dote.proto.api.activity.Activity
import kurtome.dote.proto.api.activity.ActivityList
import kurtome.dote.proto.api.activity.DoteActivity
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable_list.DotableList
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.proto.api.feed.Feed
import kurtome.dote.proto.api.feed.FeedActivityList
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.server.db.mappers.DotableMapper
import kurtome.dote.server.db.mappers.DoteMapper
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.services.DoteService
import kurtome.dote.server.services.TagService
import kurtome.dote.shared.mapper.PaginationInfoMapper
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ActivityFeedFetcher @Inject()(doteService: DoteService,
                                    dotableService: DotableService,
                                    tagService: TagService)(implicit ec: ExecutionContext)
    extends FeedFetcher
    with LogSupport {

  override def fetch(params: FeedParams): Future[Feed] = {
    assert(params.feedId.id.isActivity)
    val listLimit = params.maxItemSize
    val personId = params.loggedInUser.map(_.id)
    val activityId = params.feedId.getActivity

    val paginationInfo = PaginationInfoMapper.fromProto(activityId.getPaginationInfo)

    val list =
      if (activityId.followingOnly) {
        personId
          .map(doteService.recentDotesWithDotableFromFollowing(paginationInfo, _))
          .getOrElse(Future(Nil))
          .map(_.map(pair =>
            (DoteMapper.toProto(pair._1, Some(pair._2)), DotableMapper(pair._3, pair._4)))) map {
          list =>
            toActivityListFeedItem(params, "Recent From Following", "", list)
        }
      } else if (activityId.username.nonEmpty && params.loggedInUser.isDefined) {
        val personRow = params.loggedInUser.get
        doteService
          .recentDotesWithDotableByPerson(paginationInfo, personRow.id)
          .map(_.map(pair =>
            (DoteMapper.toProto(pair._1, Some(pair._2)), DotableMapper(pair._3, pair._4)))) map {
          list =>
            toActivityListFeedItem(params, "Recently Rated", "", list, personRow.username)
        }
      } else {
        doteService
          .readRecentDotesWithDotables(paginationInfo)
          .map(_.map(pair =>
            (DoteMapper.toProto(pair._1, Some(pair._2)), DotableMapper(pair._3, pair._4)))) map {
          list =>
            toActivityListFeedItem(params, "Recent Activity", "", list)
        }
      }

    val lists = Future.sequence(Seq(list))

    lists map { feedItems =>
      Feed(id = Some(params.feedId), items = feedItems)
    }
  }

  private def toActivityListFeedItem(feedParams: FeedParams,
                                     title: String,
                                     caption: String,
                                     list: Seq[(Dote, Dotable)],
                                     username: String = ""): FeedItem = {
    val feedList = FeedActivityList()
      .withStyle(FeedActivityList.Style.PRIMARY)
      .withActivityList(
        ActivityList(
          title = title,
          caption = caption,
          items = list.map(pair =>
            Activity().withDote(DoteActivity().withDote(pair._1).withDotable(pair._2)))))
    FeedItem()
      .withId(feedParams.feedId)
      .withContent(FeedItem.Content.ActivityList(feedList))
  }
}
