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
import kurtome.dote.proto.api.feed.FeedDotableList
import kurtome.dote.proto.api.feed.FeedFollowerSummary
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.ActivityId
import kurtome.dote.proto.api.feed.FeedId.FollowerSummaryId
import kurtome.dote.proto.api.feed.FeedId.ProfileDoteListId
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.proto.api.feed.FeedItemCommon
import kurtome.dote.proto.api.follower.FollowerSummary
import kurtome.dote.server.controllers.follow.FollowApiHelper
import kurtome.dote.server.db.mappers.DotableMapper
import kurtome.dote.server.db.mappers.DoteMapper
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.services.DoteService
import kurtome.dote.server.services.PersonService
import kurtome.dote.shared.constants.DotableKinds
import kurtome.dote.shared.constants.Emojis
import kurtome.dote.shared.mapper.PaginationInfoMapper
import kurtome.dote.shared.model.PaginationInfo
import kurtome.dote.slick.db.gen.Tables.PersonRow
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfileFeedFetcher @Inject()(dotableService: DotableService,
                                   doteService: DoteService,
                                   personService: PersonService,
                                   followApiHelper: FollowApiHelper)(implicit ec: ExecutionContext)
    extends FeedFetcher
    with LogSupport {

  override def fetch(params: FeedParams): Future[Feed] = {
    personService.readByUsername(params.feedId.getProfile.username) flatMap {
      case Some(personRow) => fetchForPerson(personRow, params)
      case None => Future(Feed.defaultInstance)
    }
  }

  def fetchForPerson(personRow: PersonRow, feedParams: FeedParams): Future[Feed] = {
    val username = personRow.username

    val smileEmojis = Emojis.smileEmojis.mkString(" ")
    val laughEmojis = Emojis.laughEmojis.mkString(" ")
    val cryEmojis = Emojis.cryEmojis.mkString(" ")
    val scowlEmojis = Emojis.scowlEmojis.mkString(" ")

    val followerSummary = followApiHelper.getSummary(personRow).map(toFollowerSummaryFeedItem)

    val paginationInfo = PaginationInfo(feedParams.maxItemSize)
    val recentActivity = doteService
      .recentDotesWithDotableByPerson(paginationInfo, personRow.id)
      .map(_.map(pair =>
        (DoteMapper.toProto(pair._1, Some(pair._2)), DotableMapper(pair._3, pair._4)))) map {
      list =>
        toActivityListFeedItem(paginationInfo, "Recently Rated", "", list, personRow)
    }

    val feedItemsFuture = Future.sequence(
      Seq(
        followerSummary,
        recentActivity
      ))

    for {
      feedItems <- feedItemsFuture
    } yield Feed(id = Some(feedParams.feedId), items = feedItems)
  }

  private def toActivityListFeedItem(paginationInfo: PaginationInfo,
                                     title: String,
                                     caption: String,
                                     list: Seq[(Dote, Dotable)],
                                     profilePerson: PersonRow): FeedItem = {
    val feedList = FeedActivityList(
      Some(
        ActivityList(
          title = title,
          caption = caption,
          items = list.map(pair =>
            Activity().withDote(DoteActivity().withDote(pair._1).withDotable(pair._2))))))
    FeedItem()
      .withId(
        FeedId().withActivity(
          ActivityId()
            .withPaginationInfo(PaginationInfoMapper.toProto(paginationInfo))
            .withUsername(profilePerson.username)))
      .withContent(FeedItem.Content.ActivityList(feedList))
  }

  private def toFollowerSummaryFeedItem(followerSummary: FollowerSummary) = {
    val itemId =
      FeedId().withFollowerSummary(
        FollowerSummaryId(username = followerSummary.getPerson.username))
    val content = FeedFollowerSummary(Some(followerSummary))
    FeedItem()
      .withCommon(FeedItemCommon(backgroundColor = FeedItemCommon.BackgroundColor.LIGHT))
      .withId(itemId)
      .withContent(FeedItem.Content.FollowerSummary(content))
  }

}
