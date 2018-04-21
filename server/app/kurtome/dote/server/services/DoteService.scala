package kurtome.dote.server.services

import java.time.{Duration, LocalDateTime}

import javax.inject._
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.proto.api.dote.Dote.EmoteKind
import kurtome.dote.server.db.DotableDbIo
import kurtome.dote.server.db.DoteDbIo
import kurtome.dote.shared.model.PaginationInfo
import kurtome.dote.shared.constants.DotableKinds.DotableKind
import kurtome.dote.shared.util.result.SuccessStatus
import kurtome.dote.shared.util.result.ActionStatus
import kurtome.dote.shared.validation.ReviewValidation
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.slick.db.DotePostgresProfile.api._
import slick.basic.BasicBackend
import slick.dbio.DBIOAction
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DoteService @Inject()(db: BasicBackend#Database,
                            dotableDbIo: DotableDbIo,
                            doteDbIo: DoteDbIo)(implicit executionContext: ExecutionContext)
    extends LogSupport {

  val popularAge = Duration.ofDays(365)
  val trendingAge = Duration.ofDays(3)

  def writeDote(personId: Long,
                dotableId: Long,
                dote: Dote,
                review: Option[String]): Future[ActionStatus] = {
    val reviewValidation = review.map(ReviewValidation.body.firstError).getOrElse(SuccessStatus)

    if (reviewValidation.isSuccess) {
      if (dote.emoteKind == EmoteKind.UNKNOWN_KIND && dote.halfStars == 0 && review.isEmpty) {
        db.run(doteDbIo.delete(personId, dotableId)).map(_ => reviewValidation)
      } else {
        val q = (for {
          doteRow <- doteDbIo.lockDoteRow(personId, dotableId)
          reviewId <- writeReviewIfExists(dotableId, doteRow.flatMap(_.reviewDotableId), review)
          _ <- doteDbIo.upsert(personId, dotableId, dote, reviewId)
        } yield ()).transactionally
        db.run(q).map(_ => reviewValidation)
      }
    } else {
      Future(reviewValidation)
    }
  }

  def readDote(personId: Long, dotableId: Long): Future[Option[Tables.DoteRow]] = {
    db.run(doteDbIo.readDote(personId, dotableId))
  }

  def doteForReview(reviewDotableId: Long): Future[Option[Tables.DoteRow]] = {
    db.run(doteDbIo.readByReviewDotableId(reviewDotableId))
  }

  def readPopular(kind: DotableKind, limit: Long): Future[Seq[Tables.DotableRow]] = {
    db.run(doteDbIo.mostPopularDotables(kind, LocalDateTime.now.minus(popularAge), limit))
  }

  def readRecentDotesWithDotables(
      paginationInfo: PaginationInfo): Future[Seq[(Tables.DoteRow,
                                                   Tables.PersonRow,
                                                   Tables.DotableRow,
                                                   Option[Tables.DotableRow],
                                                   Option[Tables.DotableRow])]] = {
    db.run(doteDbIo.recentRecentDotesWithDotables(paginationInfo))
  }

  def recentDotesWithDotableByPerson(paginationInfo: PaginationInfo,
                                     personId: Long): Future[Seq[(Tables.DoteRow,
                                                                  Tables.PersonRow,
                                                                  Tables.DotableRow,
                                                                  Option[Tables.DotableRow],
                                                                  Option[Tables.DotableRow])]] = {
    db.run(doteDbIo.recentDotesWithDotableByPerson(paginationInfo, personId))
  }

  def recentDotesWithDotableFromFollowing(
      paginationInfo: PaginationInfo,
      personId: Long): Future[Seq[(Tables.DoteRow,
                                   Tables.PersonRow,
                                   Tables.DotableRow,
                                   Option[Tables.DotableRow],
                                   Option[Tables.DotableRow])]] = {
    db.run(doteDbIo.recentDotesWithDotableFromFollowing(paginationInfo, personId))
  }

  private def writeReviewIfExists(
      dotableId: Long,
      existingReviewId: Option[Long],
      newReview: Option[String]): DBIOAction[Option[Long], NoStream, Effect.All] = {
    if (existingReviewId.isEmpty && newReview.isEmpty) {
      // No review, nothing to do
      DBIOAction.successful(None)
    } else if (existingReviewId.isEmpty && newReview.nonEmpty) {
      // New review, insert
      dotableDbIo.insertReview(dotableId, newReview.get).map(Some(_))
    } else if (existingReviewId.nonEmpty && newReview.nonEmpty) {
      // Update existing review
      dotableDbIo.updateReview(existingReviewId.get, newReview.get).map(_ => existingReviewId)
    } else {
      // Assume this is just a rating update, which does not send the review with it, leave
      // the existing review unaltered.
      // NOTE: This must return the existing ID so that the review ID isn't cleared from the
      // existing row
      DBIOAction.successful(existingReviewId)
    }
  }
}
