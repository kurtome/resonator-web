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
import resonator.server.services.PersonService

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class FollowerSummaryFeedFetcher @Inject()(
    personService: PersonService,
    followApiHelper: FollowApiHelper)(implicit ec: ExecutionContext)
    extends FeedFetcher {
  override def fetch(params: FeedParams): Future[Feed] = {
    assert(params.feedId.id.isFollowerSummary)

    for {
      feedItem <- fetchSummaryItem(params)
    } yield Feed(id = Some(params.feedId), items = Seq(feedItem))
  }

  private def fetchSummaryItem(params: FeedParams): Future[FeedItem] = {
    val username = params.feedId.getFollowerSummary.username
    for {
      person <- personService.readByUsername(username)
      summary <- followApiHelper.getSummary(person)
    } yield toFollowerSummaryFeedItem(summary)
  }

  private def toFollowerSummaryFeedItem(followerSummary: FollowerSummary) = {
    val itemId =
      FeedId().withFollowerSummary(
        FollowerSummaryId(username = followerSummary.getPerson.username))
    val content = FeedFollowerSummary(Some(followerSummary), FeedFollowerSummary.Style.PRIMARY)
    FeedItem()
      .withCommon(FeedItemCommon(backgroundColor = FeedItemCommon.BackgroundColor.LIGHT))
      .withId(itemId)
      .withContent(FeedItem.Content.FollowerSummary(content))
  }

}
