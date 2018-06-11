package kurtome.dote.server.controllers.api

import java.time.LocalDateTime
import java.time.ZoneOffset

import javax.inject._
import kurtome.dote.proto.api.action.update_radio_station_details._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.radio._
import kurtome.dote.server.controllers.radio.RadioStationFetcher
import kurtome.dote.server.services.AuthTokenService
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.services.RadioService
import kurtome.dote.server.util.UrlIds
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.ErrorStatus
import kurtome.dote.shared.util.result.StatusCodes
import kurtome.dote.shared.util.result.SuccessStatus
import kurtome.dote.slick.db.gen.Tables.RadioStationPlaylistRow
import kurtome.dote.slick.db.gen.Tables.RadioStationRow
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
