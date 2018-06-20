package resonator.server.controllers.feed

import javax.inject._
import resonator.proto.api.feed.Feed
import resonator.proto.api.feed.FeedFollowerSummary
import resonator.proto.api.feed.FeedId
import resonator.proto.api.feed.FeedId.FollowerSummaryId
import resonator.proto.api.feed.FeedItem
import resonator.proto.api.feed.FeedItemCommon
import resonator.proto.api.follower.FollowerSummary
import resonator.server.controllers.follow.FollowApiHelper
import resonator.server.services.DotableService
import resonator.server.services.DoteService
import resonator.server.services.PersonService
import resonator.shared.constants.Emojis
import resonator.shared.model.PaginationInfo
import resonator.slick.db.gen.Tables.PersonRow
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
      .map(_.map(ActivityFeedFetcher.mapActivityData)) map { list =>
      ActivityFeedFetcher.toActivityListFeedItem(
        FeedId().withActivity(FeedId.ActivityId().withUsername(username)),
        "Recently Rated",
        "",
        list)
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
