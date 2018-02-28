package kurtome.dote.server.controllers.api

import javax.inject._
import kurtome.dote.proto.api.action.get_follower_summary._
import kurtome.dote.proto.api.follower.FollowerSummary
import kurtome.dote.proto.api.person.Person
import kurtome.dote.server.controllers.follow.FollowApiHelper
import kurtome.dote.server.controllers.mappers.PersonMapper
import kurtome.dote.server.services._
import kurtome.dote.server.util.UrlIds
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.ActionStatus
import kurtome.dote.shared.util.result.ErrorStatus
import kurtome.dote.shared.util.result.StatusCodes
import kurtome.dote.shared.util.result.SuccessStatus
import kurtome.dote.slick.db.gen.Tables
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
