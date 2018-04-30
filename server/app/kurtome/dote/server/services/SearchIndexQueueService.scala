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

  def readDotableRow(): Future[Tables.SearchIndexQueueRow] = {
    db.run(
      Tables.SearchIndexQueue
        .filter(_.indexName === "dotables")
        .result
        .head)
  }

  def writeDotableRow(time: LocalDateTime, lastBatchMaxId: Long): Future[Unit] = {
    db.run(
        Tables.SearchIndexQueue
          .filter(_.indexName === "dotables")
          .map(row => (row.syncCompletedThroughTime, row.lastBatchMaxId))
          .update((time, lastBatchMaxId))
      )
      .map(_ => Unit)
  }

}
