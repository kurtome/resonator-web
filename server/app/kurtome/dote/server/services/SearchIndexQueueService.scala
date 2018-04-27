package kurtome.dote.server.services

import java.time.Duration
import java.time.LocalDateTime

import javax.inject._
import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.gen.Tables
import slick.basic.BasicBackend
import slick.dbio.DBIOAction
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class SearchIndexQueueService @Inject()(db: BasicBackend#Database)(
    implicit executionContext: ExecutionContext)
    extends LogSupport {

  def readDotableSyncCompletedTimestamp(): Future[LocalDateTime] = {
    db.run(
      Tables.SearchIndexQueue
        .filter(_.indexName === "dotables")
        .map(_.syncCompletedThroughTime)
        .result
        .head)
  }

  def writeDotableSyncCompletedTimestamp(time: LocalDateTime): Future[Unit] = {
    db.run(
        Tables.SearchIndexQueue
          .filter(_.indexName === "dotables")
          .map(_.syncCompletedThroughTime)
          .update(time)
      )
      .map(_ => Unit)
  }

}
