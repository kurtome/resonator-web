package kurtome.dote.server.controllers.feed

import javax.inject._
import kurtome.dote.proto.api.feed.Feed
import kurtome.dote.proto.api.feed.FeedFollowerSummary
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.FollowerSummaryId
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.proto.api.feed.FeedItemCommon
import kurtome.dote.proto.api.follower.FollowerSummary
import kurtome.dote.server.controllers.follow.FollowApiHelper
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.services.DoteService
import kurtome.dote.server.services.PersonService
import kurtome.dote.shared.constants.Emojis
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
