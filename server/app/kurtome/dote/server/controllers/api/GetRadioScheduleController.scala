package kurtome.dote.server.controllers.api

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

import javax.inject._
import kurtome.dote.proto.api.action.get_radio_schedule._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.radio._
import kurtome.dote.server.services.AuthTokenService
import kurtome.dote.server.services.RadioService
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.StatusCodes
import kurtome.dote.shared.util.result.SuccessStatus
import kurtome.dote.slick.db.gen.Tables.RadioStationRow
import kurtome.dote.slick.db.gen.Tables.RadioStationPlaylistRow
import play.api.mvc._
import wvlet.log.LogSupport

import scala.concurrent._

@Singleton
class GetRadioScheduleController @Inject()(
    cc: ControllerComponents,
    radioService: RadioService,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetRadioScheduleRequest, GetRadioScheduleResponse](cc)
    with LogSupport {

  override def parseRequest(bytes: Array[Byte]) =
    GetRadioScheduleRequest.parseFrom(bytes)

  override def action(request: Request[GetRadioScheduleRequest]) = {
    val requestServerTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli
    val requestClientTime = request.body.requestTimeMillis
    // this is sloppy due to request overhead, but should give a roughly correct offset
    val offsetMillis = requestClientTime - requestServerTime

    authTokenService.simplifiedRead(request) flatMap { loggedInPerson =>
      radioService.readAllStationsWithCurrentSchedule() map { stationsWithCurrentSchedule =>
        GetRadioScheduleResponse()
          .withResponseStatus(StatusMapper.toProto(SuccessStatus))
          .withAmStations(filterStationsToProto(offsetMillis, stationsWithCurrentSchedule, "AM"))
          .withFmStations(filterStationsToProto(offsetMillis, stationsWithCurrentSchedule, "FM"))
      }
    }

  }

  private def filterStationsToProto(
      offsetMillis: Long,
      stationsWithCurrentSchedule: Map[RadioStationRow, Seq[(RadioStationPlaylistRow, Dotable)]],
      frequencyKind: String) = {
    stationsWithCurrentSchedule
      .filter(_._1.frequencyKind == frequencyKind)
      .map(toRadioStationSchedule(offsetMillis))
      .toSeq
      .sortBy(_.getStation.frequency)
  }

  private def toRadioStationSchedule(offsetMillis: Long)(
      stationWithPlaylist: (RadioStationRow, Seq[(RadioStationPlaylistRow, Dotable)]))
    : RadioStationSchedule = {
    val station = stationWithPlaylist._1
    val episodes = stationWithPlaylist._2
    RadioStationSchedule()
      .withStation(
        RadioStation(
          callSign = station.callSign,
          frequency = station.frequency.toFloat,
          frequencyKind = station.frequencyKind match {
            case "AM" => RadioStation.FrequencyKind.AM
            case "FM" => RadioStation.FrequencyKind.AM
            case _ => RadioStation.FrequencyKind.UNKNOWN_KIND
          }
        ))
      .withScheduledEpisodes(episodes map {
        case (scheduled, ep) =>
          ScheduledEpisode(
            startTimeMillis = scheduled.startTime
              .toInstant(ZoneOffset.UTC)
              .toEpochMilli + offsetMillis,
            endTimeMillis = scheduled.endTime.toInstant(ZoneOffset.UTC).toEpochMilli + offsetMillis
          ).withEpisode(ep)
      })
  }

}
