package resonator.server.controllers.feed

import javax.inject._
import resonator.proto.api.activity.Activity
import resonator.proto.api.activity.ActivityList
import resonator.proto.api.activity.DoteActivity
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.dotable_list.DotableList
import resonator.proto.api.dote.Dote
import resonator.proto.api.feed.Feed
import resonator.proto.api.feed.FeedActivityList
import resonator.proto.api.feed.FeedId
import resonator.proto.api.feed.FeedItem
import resonator.proto.api.feed.FeedItemCommon
import resonator.server.db.mappers.DotableMapper
import resonator.server.db.mappers.DoteMapper
import resonator.server.services.DotableService
import resonator.server.services.DoteService
import resonator.server.services.PersonService
import resonator.server.services.TagService
import resonator.shared.mapper.PaginationInfoMapper
import resonator.slick.db.gen.Tables
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class ActivityFeedFetcher @Inject()(personService: PersonService,
                                    doteService: DoteService,
                                    dotableService: DotableService,
                                    tagService: TagService)(implicit ec: ExecutionContext)
    extends FeedFetcher
    with LogSupport {

  import ActivityFeedFetcher._

  override def fetch(params: FeedParams): Future[Feed] = {
    assert(params.feedId.id.isActivity)
    val listLimit = params.maxItemSize
    val loggedInPersonId = params.loggedInUser.map(_.id)
    val activityId = params.feedId.getActivity

    val paginationInfo = PaginationInfoMapper.fromProto(activityId.getPaginationInfo)

    val list =
      if (activityId.followingOnly) {
        loggedInPersonId
          .map(doteService.recentDotesWithDotableFromFollowing(paginationInfo, _))
          .getOrElse(Future(Nil))
          .map(_.map(mapActivityData)) map { list =>
          toActivityListFeedItem(params.feedId,
                                 "Recent From Following",
                                 "",
                                 list,
                                 style = FeedActivityList.Style.PRIMARY)
        }
      } else if (activityId.username.nonEmpty) {
        (personService.readByUsername(activityId.username) flatMap {
          case Some(personRow) => {
            doteService
              .recentDotesWithDotableByPerson(paginationInfo, personRow.id)
              .map(_.map(mapActivityData))
          }
          case _ => Future(Nil)
        }) map { list =>
          toActivityListFeedItem(params.feedId,
                                 "Recently Rated",
                                 "",
                                 list,
                                 style = FeedActivityList.Style.PRIMARY)
        }
      } else {
        doteService
          .readRecentDotesWithDotables(paginationInfo)
          .map(_.map(mapActivityData)) map { list =>
          toActivityListFeedItem(params.feedId,
                                 "Recent Activity",
                                 "",
                                 list,
                                 style = FeedActivityList.Style.PRIMARY)
        }
      }

    val lists = Future.sequence(Seq(list))

    lists map { feedItems =>
      Feed(id = Some(params.feedId), items = feedItems)
    }
  }

}

object ActivityFeedFetcher {

  def toActivityListFeedItem(feedId: FeedId,
                             title: String,
                             caption: String,
                             list: Seq[(Dote, Dotable, Option[Dotable])],
                             style: FeedActivityList.Style = FeedActivityList.Style.SUMMARY,
                             backgroundColor: FeedItemCommon.BackgroundColor =
                               FeedItemCommon.BackgroundColor.DEFAULT): FeedItem = {
    val feedList = FeedActivityList()
      .withStyle(style)
      .withActivityList(
        ActivityList(title = title,
                     caption = caption,
                     items = list.map(pair =>
                       Activity().withDote(
                         DoteActivity(review = pair._3).withDote(pair._1).withDotable(pair._2)))))
    FeedItem()
      .withCommon(FeedItemCommon().withBackgroundColor(backgroundColor))
      .withId(feedId)
      .withContent(FeedItem.Content.ActivityList(feedList))
  }

  def mapActivityData(
      pair: (Tables.DoteRow,
             Tables.PersonRow,
             Tables.DotableRow,
             Option[Tables.DotableRow],
             Option[Tables.DotableRow])) = {
    (DoteMapper.toProto(pair._1, Some(pair._2)),
     DotableMapper(pair._3, pair._4),
     pair._5.map(DotableMapper(_, None)))
  }
}
