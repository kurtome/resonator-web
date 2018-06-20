package resonator.server.services

import javax.inject._
import resonator.proto.api.dotable.Dotable
import resonator.server.db.RadioDbIo
import resonator.slick.db.gen.Tables.RadioStationPlaylistRow
import resonator.slick.db.gen.Tables.RadioStationRow
import slick.basic.BasicBackend
import wvlet.log.LogSupport

import scala.concurrent._

@Singleton
class RadioService @Inject()(db: BasicBackend#Database, radioDbIo: RadioDbIo)(
    implicit ec: ExecutionContext)
    extends LogSupport {

  def readStationByCallSign(callSign: String): Future[Option[RadioStationRow]] = {
    db.run(radioDbIo.readStationByCallSign(callSign))
  }

  def readLatestPlaylistEntry(stationId: Long): Future[Option[RadioStationPlaylistRow]] = {
    db.run(radioDbIo.readLatestPlaylistEntry(stationId))
  }

  def readAllStations(): Future[Seq[RadioStationRow]] = {
    db.run(radioDbIo.readAllStations())
  }

  def readPodcastsForStation(stationId: Long): Future[Seq[Long]] = {
    db.run(radioDbIo.readPodcastIdsForStation(stationId))
  }

  def createStationPlaylistEntry(entry: RadioStationPlaylistRow): Future[Unit] = {
    db.run(radioDbIo.createStationPlaylistEntry(entry)).map(_ => Unit)
  }

  def readAllStationsWithCurrentSchedule()
    : Future[Map[RadioStationRow, Seq[(RadioStationPlaylistRow, Dotable)]]] = {
    db.run(radioDbIo.readAllStationsWithCurrentSchedule())
  }

  def addPodcastToStation(stationId: Long, podcastId: Long): Future[Unit] = {
    db.run(radioDbIo.addRadioStationPodcast(stationId, podcastId)).map(_ => Unit)
  }

  def removePodcastFromStation(stationId: Long, podcastId: Long): Future[Unit] = {
    db.run(radioDbIo.deleteRadioStationPodcast(stationId, podcastId)).map(_ => Unit)
  }

}
