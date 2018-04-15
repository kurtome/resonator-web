package kurtome.dote.server.services

import java.time.{Duration, LocalDateTime}

import javax.inject._
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.proto.api.dote.Dote.EmoteKind
import kurtome.dote.server.db.DoteDbIo
import kurtome.dote.shared.model.PaginationInfo
import kurtome.dote.shared.constants.DotableKinds.DotableKind
import kurtome.dote.slick.db.gen.Tables
import slick.basic.BasicBackend

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DoteService @Inject()(db: BasicBackend#Database, doteDbIo: DoteDbIo)(
    implicit executionContext: ExecutionContext) {

  val popularAge = Duration.ofDays(365)
  val trendingAge = Duration.ofDays(3)

  def writeDote(personId: Long, dotableId: Long, dote: Dote): Future[Unit] = {
    if (dote.emoteKind == EmoteKind.UNKNOWN_KIND && dote.halfStars == 0) {
      db.run(doteDbIo.delete(personId, dotableId)).map(_ => Unit)
    } else {
      db.run(doteDbIo.upsert(personId, dotableId, dote)).map(_ => Unit)
    }
  }

  def readDote(personId: Long, dotableId: Long): Future[Option[Tables.DoteRow]] = {
    db.run(doteDbIo.readDote(personId, dotableId))
  }

  def readPopular(kind: DotableKind, limit: Long): Future[Seq[Tables.DotableRow]] = {
    db.run(doteDbIo.mostPopularDotables(kind, LocalDateTime.now.minus(popularAge), limit))
  }

  def readRecentDotesWithDotables(paginationInfo: PaginationInfo): Future[
    Seq[(Tables.DoteRow, Tables.PersonRow, Tables.DotableRow, Option[Tables.DotableRow])]] = {
    db.run(doteDbIo.recentRecentDotesWithDotables(paginationInfo))
  }

  def recentDotesWithDotableByPerson(paginationInfo: PaginationInfo, personId: Long): Future[
    Seq[(Tables.DoteRow, Tables.PersonRow, Tables.DotableRow, Option[Tables.DotableRow])]] = {
    db.run(doteDbIo.recentDotesWithDotableByPerson(paginationInfo, personId))
  }

  def recentDotesWithDotableFromFollowing(paginationInfo: PaginationInfo, personId: Long): Future[
    Seq[(Tables.DoteRow, Tables.PersonRow, Tables.DotableRow, Option[Tables.DotableRow])]] = {
    db.run(doteDbIo.recentDotesWithDotableFromFollowing(paginationInfo, personId))
  }
}
