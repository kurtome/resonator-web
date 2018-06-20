package resonator.server.controllers.api

import javax.inject._
import resonator.proto.api.action.get_feed._
import resonator.proto.api.feed.Feed
import resonator.proto.api.feed.FeedId.Id
import resonator.server.controllers.feed.ActivityFeedFetcher
import resonator.server.controllers.feed.FeedFetcher
import resonator.server.controllers.feed.FeedParams
import resonator.server.controllers.feed.FollowerSummaryFeedFetcher
import resonator.server.controllers.feed.HomeFeedFetcher
import resonator.server.controllers.feed.ProfileFeedFetcher
import resonator.server.controllers.feed.SecondaryDotableDetailsFeedFetcher
import resonator.server.controllers.feed.TagListFeedFetcher
import resonator.server.services.{AuthTokenService, DotableService}
import resonator.shared.mapper.StatusMapper
import resonator.shared.util.result.SuccessStatus
import play.api.mvc._
import wvlet.log.LogSupport

import scala.concurrent._

@Singleton
class GetFeedController @Inject()(
    cc: ControllerComponents,
    podcastDbService: DotableService,
    homeFeedFetcher: HomeFeedFetcher,
    profileFeedFetcher: ProfileFeedFetcher,
    tagListFeedFetcher: TagListFeedFetcher,
    followerSummaryFeedFetcher: FollowerSummaryFeedFetcher,
    activityFeedFetcher: ActivityFeedFetcher,
    secondaryDotableDetailsFeedFetcher: SecondaryDotableDetailsFeedFetcher,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetFeedRequest, GetFeedResponse](cc)
    with LogSupport {

  override def parseRequest(bytes: Array[Byte]) =
    GetFeedRequest.parseFrom(bytes)

  override def action(request: Request[GetFeedRequest]) = {
    authTokenService.simplifiedRead(request) flatMap { loggedInPerson =>
      val personId = loggedInPerson.map(_.id)

      val feedId = request.body.getId
      val feedParams = FeedParams(loggedInPerson, request.body.maxItemSize, feedId)

      debug(s"fetching $feedId")
      val fetcher = feedId.id match {
        case Id.Home(_) => homeFeedFetcher
        case Id.Profile(_) => profileFeedFetcher
        case Id.TagList(_) => tagListFeedFetcher
        case Id.FollowerSummary(_) => followerSummaryFeedFetcher
        case Id.Activity(_) => activityFeedFetcher
        case Id.SecondaryDotableDetails(_) => secondaryDotableDetailsFeedFetcher
        case _ =>
          new FeedFetcher with LogSupport {
            override def fetch(params: FeedParams): Future[Feed] = {
              warn("fetching non-existent feed")
              Future(Feed.defaultInstance)
            }
          }
      }
      fetchFeed(fetcher, feedParams)
    }
  }

  private def fetchFeed(feedFetcher: FeedFetcher,
                        feedParams: FeedParams): Future[GetFeedResponse] = {
    feedFetcher.fetch(feedParams) map { feed =>
      GetFeedResponse(Some(StatusMapper.toProto(SuccessStatus)), Some(feed))
    }
  }

}
