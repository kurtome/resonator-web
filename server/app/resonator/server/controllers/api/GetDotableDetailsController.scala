package resonator.server.controllers.api

import javax.inject._
import resonator.proto.api.action.get_dotable._
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.dotable_list.DotableList
import resonator.proto.api.feed.Feed
import resonator.proto.api.feed.FeedDotableList
import resonator.proto.api.feed.FeedId
import resonator.proto.api.feed.FeedId.SecondaryDotableDetailsId
import resonator.proto.api.feed.FeedItem
import resonator.server.search.SearchClient
import resonator.server.services.AuthTokenService
import resonator.server.services.DotableService
import resonator.server.util.UrlIds
import resonator.server.util.UrlIds.IdKinds
import resonator.shared.mapper.StatusMapper
import resonator.shared.util.result.ErrorStatus
import resonator.shared.util.result.StatusCodes
import resonator.shared.util.result.SuccessStatus
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class GetDotableDetailsController @Inject()(
    cc: ControllerComponents,
    authTokenService: AuthTokenService,
    dotableService: DotableService,
    searchClient: SearchClient)(implicit ec: ExecutionContext)
    extends ProtobufController[GetDotableDetailsRequest, GetDotableDetailsResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    GetDotableDetailsRequest.parseFrom(bytes)

  override def action(request: Request[GetDotableDetailsRequest]) =
    authTokenService.simplifiedRead(request) flatMap { loggedInPerson =>
      val dotableId = UrlIds.decode(IdKinds.Dotable, request.body.id)
      for {
        dotableOpt <- dotableService.readDotableDetails(dotableId, loggedInPerson.map(_.id))
      } yield
        dotableOpt match {
          case Some(dotable) => {
            GetDotableDetailsResponse(Some(StatusMapper.toProto(SuccessStatus)), Some(dotable))
              .withFeedId(
                FeedId().withSecondaryDotableDetails(SecondaryDotableDetailsId(request.body.id)))
          }
          case None =>
            GetDotableDetailsResponse(
              Some(StatusMapper.toProto(ErrorStatus(StatusCodes.NotFound))),
              None)
        }
    }

}
