package kurtome.dote.server.db

import java.time.LocalDateTime

import javax.inject._
import kurtome.dote.server.db.mappers.DotableMapper
import kurtome.dote.shared.constants.DotableKinds
import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.slick.db.gen.Tables.RadioStationPlaylistRow
import slick.lifted.Compiled

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class RadioDbIo @Inject()(implicit executionContext: ExecutionContext) {

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

}
