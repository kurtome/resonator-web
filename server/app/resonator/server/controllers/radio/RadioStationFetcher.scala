package resonator.server.controllers.radio

import javax.inject._
import resonator.proto.api.radio.RadioStationDetails
import resonator.server.db.mappers.RadioStationMapper
import resonator.server.services.DotableService
import resonator.server.services.RadioService
import wvlet.log.LogSupport

import scala.concurrent._

@Singleton
class RadioStationFetcher @Inject()(radioService: RadioService, dotableService: DotableService)(
    implicit ec: ExecutionContext)
    extends LogSupport {

  def fetch(callSign: String) = {
    for {
      stationOpt <- radioService.readStationByCallSign(callSign)
      podcastIds <- stationOpt match {
        case Some(station) => radioService.readPodcastsForStation(station.id)
        case _ => Future(Nil)
      }
      podcasts <- dotableService.readBatchById(podcastIds)
    } yield
      stationOpt map { station =>
        RadioStationDetails(station = Some(RadioStationMapper.toProto(station)),
                            podcasts = podcasts)
      }
  }

}
