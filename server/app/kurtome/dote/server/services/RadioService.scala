package kurtome.dote.server.services

import javax.inject._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.server.db.RadioDbIo
import kurtome.dote.slick.db.gen.Tables.RadioStationPlaylistRow
import kurtome.dote.slick.db.gen.Tables.RadioStationRow
import slick.basic.BasicBackend
import wvlet.log.LogSupport

import scala.concurrent._

@Singleton
class RadioService @Inject()(db: BasicBackend#Database, radioDbIo: RadioDbIo)(
    implicit ec: ExecutionContext)
    extends LogSupport {

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

}
