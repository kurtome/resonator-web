package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.proto.api.action.get_feed._
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.server.controllers.feed.FeedParams
import kurtome.dote.server.controllers.feed.HomeFeedFetcher
import kurtome.dote.server.controllers.feed.ProfileFeedFetcher
import kurtome.dote.server.services.{AuthTokenService, DotableService}
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.SuccessStatus
import play.api.mvc._
import wvlet.log.LogSupport

import scala.concurrent._

@Singleton
class GetFeedController @Inject()(
    cc: ControllerComponents,
    podcastDbService: DotableService,
    homeFeedFetcher: HomeFeedFetcher,
    profileFeedFetcher: ProfileFeedFetcher,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetFeedRequest, GetFeedResponse](cc)
    with LogSupport {

  override def parseRequest(bytes: Array[Byte]) =
    GetFeedRequest.parseFrom(bytes)

  override def action(request: Request[GetFeedRequest]) = {
    authTokenService.simplifiedRead(request) flatMap { loggedInPerson =>
      // TODO: refactor this so it's easier to use the logged in person's ID
      val personId = loggedInPerson.map(_.id)

      val feedId = request.body.getId
      val feedParams = FeedParams(loggedInPerson, request.body.maxItemSize, feedId)

      debug(s"fetching $feedId")
      val fetch = feedId.id match {
        case FeedId.Id.HomeId(id) => fetchHomeFeed _
        case FeedId.Id.ProfileId(id) => fetchProfileFeed _
        case FeedId.Id.Empty => noFeed _
      }
      fetch(feedParams)
    }
  }

  private def noFeed(feedParams: FeedParams) = {
    warn("fetching non-existent feed")
    Future(GetFeedResponse.defaultInstance)
  }

  private def fetchHomeFeed(feedParams: FeedParams) = {
    homeFeedFetcher.fetch(feedParams) map { feed =>
      GetFeedResponse(Some(StatusMapper.toProto(SuccessStatus)), Some(feed))
    }
  }

  private def fetchProfileFeed(feedParams: FeedParams) = {
    profileFeedFetcher.fetch(feedParams) map { feed =>
      GetFeedResponse(Some(StatusMapper.toProto(SuccessStatus)), Some(feed))
    }
  }

}
