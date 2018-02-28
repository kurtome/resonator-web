package kurtome.dote.server.controllers.api

import javax.inject._
import kurtome.dote.proto.api.action.set_follow._
import kurtome.dote.proto.api.follower.FollowerSummary
import kurtome.dote.server.controllers.follow.FollowApiHelper
import kurtome.dote.server.services._
import kurtome.dote.server.util.UrlIds
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.FailedData
import kurtome.dote.shared.util.result.SuccessData
import kurtome.dote.shared.util.result.UnknownErrorStatus
import kurtome.dote.shared.util.result.ActionStatus
import kurtome.dote.shared.util.result.ErrorStatus
import kurtome.dote.shared.util.result.StatusCodes
import kurtome.dote.shared.util.result.SuccessStatus
import play.api.mvc._

import scala.concurrent._

@Singleton
class SetFollowController @Inject()(
    cc: ControllerComponents,
    personService: PersonService,
    followerService: FollowerService,
    followApiHelper: FollowApiHelper,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext)
    extends ProtobufController[SetFollowRequest, SetFollowResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    SetFollowRequest.parseFrom(bytes)

  override def action(request: Request[SetFollowRequest]) = {
    authTokenService.readLoggedInPersonFromCookie(request) flatMap {
      case SuccessData(Some(person)) =>
        if (person.id == UrlIds.decodePerson(request.body.requesterPersonId)) {
          request.body.requestedState match {
            case SetFollowRequest.State.FOLLOWING =>
              follow(person.id, UrlIds.decodePerson(request.body.followPersonId))
            case SetFollowRequest.State.NOT_FOLLOWING =>
              unfollow(person.id, UrlIds.decodePerson(request.body.followPersonId))
            case _ => Future(response(UnknownErrorStatus))
          }
        } else {
          // Person setting the follow doesn't match the logged in person
          Future(response(ErrorStatus(StatusCodes.InvalidAuthentication)))
        }
      case FailedData(_, error) => Future(response(error))
      case _ => Future(response(UnknownErrorStatus))
    }
  }

  private def follow(followerId: Long, followeeId: Long): Future[SetFollowResponse] = {
    for {
      followStatus <- followerService.follow(followerId, followeeId)
      followeePerson <- personService.readById(followeeId)
      summary <- followApiHelper.getSummary(followeePerson)
    } yield response(followStatus, summary)
  }

  private def unfollow(followerId: Long, followeeId: Long): Future[SetFollowResponse] = {
    for {
      followStatus <- followerService.unfollow(followerId, followeeId)
      followeePerson <- personService.readById(followeeId)
      summary <- followApiHelper.getSummary(followeePerson)
    } yield response(followStatus, summary)
  }

  private def response(status: ActionStatus,
                       summary: FollowerSummary = FollowerSummary.defaultInstance) = {
    SetFollowResponse(Some(StatusMapper.toProto(status)), Some(summary))
  }

}
