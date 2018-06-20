package resonator.server.controllers.api

import java.time.LocalDateTime
import java.time.ZoneOffset

import javax.inject._
import resonator.proto.api.action.update_radio_station_details._
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.radio._
import resonator.server.controllers.radio.RadioStationFetcher
import resonator.server.services.AuthTokenService
import resonator.server.services.DotableService
import resonator.server.services.RadioService
import resonator.server.util.UrlIds
import resonator.shared.mapper.StatusMapper
import resonator.shared.util.result.ErrorStatus
import resonator.shared.util.result.StatusCodes
import resonator.shared.util.result.SuccessStatus
import resonator.slick.db.gen.Tables.RadioStationPlaylistRow
import resonator.slick.db.gen.Tables.RadioStationRow
import play.api.mvc._
import wvlet.log.LogSupport

import scala.concurrent._

@Singleton
class UpdateRadioStationController @Inject()(
    cc: ControllerComponents,
    radioService: RadioService,
    dotableService: DotableService,
    radioStationFetcher: RadioStationFetcher,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext)
    extends ProtobufController[UpdateRadioStationDetailsRequest,
                               UpdateRadioStationDetailsResponse](cc)
    with LogSupport {

  override def parseRequest(bytes: Array[Byte]) =
    UpdateRadioStationDetailsRequest.parseFrom(bytes)

  override def action(request: Request[UpdateRadioStationDetailsRequest]) =
    authTokenService.requireAdmin(request) flatMap { _ =>
      for {
        stationOpt <- radioService.readStationByCallSign(request.body.callSign)
        _ <- stationOpt match {
          case Some(station) => updateAction(station.id, request.body.action)
          case _ => Future.unit
        }
        detailsOpt <- radioStationFetcher.fetch(request.body.callSign)
      } yield
        UpdateRadioStationDetailsResponse(stationDetails = detailsOpt)
          .withResponseStatus(StatusMapper.toProto(detailsOpt match {
            case Some(_) => SuccessStatus
            case None => ErrorStatus(StatusCodes.NotFound)
          }))
    }

  private def updateAction(stationId: Long,
                           action: UpdateRadioStationDetailsRequest.Action): Future[Unit] = {
    action match {
      case UpdateRadioStationDetailsRequest.Action.AddPodcastId(podcastId) => {
        val dotableId = UrlIds.decodeDotable(podcastId)
        for {
          podcast <- dotableService.readDotableShallow(dotableId)
          _ = assert(podcast.isDefined && podcast.get.kind == Dotable.Kind.PODCAST)
          _ <- radioService.addPodcastToStation(stationId, dotableId)
        } yield ()
      }
      case UpdateRadioStationDetailsRequest.Action.RemovePodcastId(podcastId) => {
        val dotableId = UrlIds.decodeDotable(podcastId)
        for {
          podcast <- dotableService.readDotableShallow(dotableId)
          _ = assert(podcast.isDefined && podcast.get.kind == Dotable.Kind.PODCAST)
          _ <- radioService.removePodcastFromStation(stationId, dotableId)
        } yield ()
      }
      case _ => {
        throw new IllegalArgumentException("Unexpected action.")
      }
    }
  }

}
