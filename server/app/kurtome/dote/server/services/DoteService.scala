package kurtome.dote.server.services

import java.time.{Duration, LocalDateTime}
import javax.inject._

import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.server.db.DoteDbIo
import kurtome.dote.slick.db.DotableKinds.DotableKind
import kurtome.dote.slick.db.gen.Tables
import slick.basic.BasicBackend

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DoteService @Inject()(db: BasicBackend#Database, doteDbIo: DoteDbIo)(
    implicit executionContext: ExecutionContext) {

  val popularAge = Duration.ofDays(365)
  val trendingAge = Duration.ofDays(3)

  def writeDote(personId: Long, dotableId: Long, dote: Dote): Future[Unit] = {
    db.run(doteDbIo.upsert(personId, dotableId, dote)).map(_ => Unit)
  }

  def readPopular(kind: DotableKind, limit: Long): Future[Seq[Tables.DotableRow]] = {
    db.run(doteDbIo.mostPopularDotables(kind, LocalDateTime.now.minus(popularAge), limit))
  }

  def readRecentDotesWithDotables(limit: Long): Future[
    Seq[(Tables.DoteRow, Tables.PersonRow, Tables.DotableRow, Option[Tables.DotableRow])]] = {
    db.run(doteDbIo.recentRecentDotesWithDotables(limit))
  }
}
