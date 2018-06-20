package resonator.server.controllers.api

import javax.inject._
import resonator.proto.api.action.get_follower_summary._
import resonator.proto.api.follower.FollowerSummary
import resonator.proto.api.person.Person
import resonator.server.controllers.follow.FollowApiHelper
import resonator.server.controllers.mappers.PersonMapper
import resonator.server.services._
import resonator.server.util.UrlIds
import resonator.shared.mapper.StatusMapper
import resonator.shared.util.result.ActionStatus
import resonator.shared.util.result.ErrorStatus
import resonator.shared.util.result.StatusCodes
import resonator.shared.util.result.SuccessStatus
import resonator.slick.db.gen.Tables
import play.api.mvc._

import scala.concurrent._

@Singleton
class GetFollowerSummaryController @Inject()(
    cc: ControllerComponents,
    personService: PersonService,
    followApiHelper: FollowApiHelper,
    followerService: FollowerService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetFollowerSummaryRequest, GetFollowerSummaryResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    GetFollowerSummaryRequest.parseFrom(bytes)

  override def action(request: Request[GetFollowerSummaryRequest]) = {
    for {
      person <- personService.readByUsername(request.body.username)
      summary <- followApiHelper.getSummary(person)
      status = getStatus(person)
    } yield response(status, summary)
  }

  private def getStatus(person: Option[Tables.PersonRow]) = {
    if (person.isDefined) {
      SuccessStatus
    } else {
      ErrorStatus(StatusCodes.NotFound)
    }
  }

  private def response(status: ActionStatus, summary: FollowerSummary) = {
    GetFollowerSummaryResponse(Some(StatusMapper.toProto(status)), Some(summary))
  }

}
