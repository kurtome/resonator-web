package kurtome.dote.server.controllers.feed

import javax.inject._
import kurtome.dote.proto.api.feed.Feed
import kurtome.dote.proto.api.feed.FeedFollowerSummary
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.FollowerSummaryId
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.proto.api.follower.FollowerSummary
import kurtome.dote.server.controllers.follow.FollowApiHelper
import kurtome.dote.server.services.PersonService

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
      .withId(itemId)
      .withContent(FeedItem.Content.FollowerSummary(content))
  }

}
