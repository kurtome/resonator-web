package resonator.server.db

import java.time.LocalDateTime

import javax.inject._
import resonator.server.db.mappers.DotableMapper
import resonator.server.services.LoginCodeService
import resonator.shared.constants.DotableKinds
import resonator.slick.db.DotePostgresProfile.api._
import resonator.slick.db.gen.Tables
import resonator.slick.db.gen.Tables.RadioStationPlaylistRow
import slick.lifted.Compiled
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class RadioDbIo @Inject()(implicit executionContext: ExecutionContext) extends LogSupport {
  object Queries {
    val readPodcastIdsForStations = Compiled { (stationId: Rep[Long]) =>
      Tables.RadioStationPodcast.filter(_.stationId === stationId).map(_.podcastId)
    }

    val readStationPlaylistPodcastEnties = Compiled {
      (stationId: Rep[Long], podcastId: Rep[Long], cutoffDate: Rep[LocalDateTime]) =>
        for {
          (playlist, _) <- Tables.RadioStationPlaylist.filter(_.stationId === stationId) join Tables.Dotable.filter(
            row =>
              row.parentId === podcastId && row.kind === DotableKinds.PodcastEpisode) on (_.episodeId === _.id)
        } yield playlist
    }

    val readLatestPlaylistEntry = Compiled { (stationId: Rep[Long]) =>
      Tables.RadioStationPlaylist
        .filter(_.stationId === stationId)
        .sortBy(_.startTime.desc)
        .take(1)
    }

    val readAllStationsWithCurrentSchedule = Compiled { (endTimeCutoff: Rep[LocalDateTime]) =>
      for {
        (station, playlist) <- Tables.RadioStation.filter(_.enabled) join Tables.RadioStationPlaylist
          .filter(_.endTime > endTimeCutoff) on (_.id === _.stationId)
        episode <- Tables.Dotable if playlist.episodeId === episode.id
        podcast <- Tables.Dotable if episode.parentId === podcast.id
      } yield (station, playlist, episode, podcast)
    }

    val readStationByCallSign = Compiled { (callSign: Rep[String]) =>
      Tables.RadioStation.filter(_.callSign === callSign)
    }
  }

  def readPodcastIdsForStation(stationId: Long) = {
    Queries.readPodcastIdsForStations(stationId).result
  }

  def readAllStations() = {
    Tables.RadioStation.filter(_.enabled).result
  }

  def readStationPlaylistPodcastEnties(stationId: Long,
                                       podcastId: Long,
                                       cutoffTime: LocalDateTime) = {
    Queries.readStationPlaylistPodcastEnties(stationId, podcastId, cutoffTime).result
  }

  def createStationPlaylistEntry(entry: Tables.RadioStationPlaylistRow) = {
    assert(entry.startTime.isBefore(entry.endTime))
    Tables.RadioStationPlaylist += entry
  }

  def readLatestPlaylistEntry(stationId: Long) = {
    Queries.readLatestPlaylistEntry(stationId).result.map(_.headOption)
  }

  def readAllStationsWithCurrentSchedule() = {
    Queries
      .readAllStationsWithCurrentSchedule(LocalDateTime.now())
      .result
      .map(_.groupBy(_._1)) map { grouped =>
      grouped.map {
        case (station, results) => {
          (station, results.map(tup => tup._2 -> DotableMapper(tup._3, Some(tup._4))))
        }
      }
    }
  }

  def readStationByCallSign(callSign: String) = {
    Queries.readStationByCallSign(callSign).result.headOption
  }

  def deleteRadioStationPodcast(stationId: Long, podcastId: Long) = {
    Tables.RadioStationPodcast
      .filter(row => row.stationId === stationId && row.podcastId === podcastId)
      .delete
  }

  def addRadioStationPodcast(stationId: Long, podcastId: Long) = {
    Tables.RadioStationPodcast += Tables.RadioStationPodcastRow(stationId, podcastId)
  }

}
