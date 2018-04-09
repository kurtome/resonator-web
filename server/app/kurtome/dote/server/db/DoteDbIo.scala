package kurtome.dote.server.db

import java.time.LocalDateTime

import javax.inject._
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.shared.constants.EmoteKinds
import kurtome.dote.shared.mapper.EmoteKindMapper
import kurtome.dote.shared.model.PaginationInfo
import kurtome.dote.slick.db.DotableKinds.DotableKind
import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.shared.constants.EmoteKinds.EmoteKind
import kurtome.dote.slick.db.gen.Tables
import slick.lifted.Compiled

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class DoteDbIo @Inject()(implicit executionContext: ExecutionContext) {

  object Queries {
    val filterByPersonAndDotableId = Compiled { (personId: Rep[Long], dotableId: Rep[Long]) =>
      Tables.Dote.filter(row => row.personId === personId && row.dotableId === dotableId)
    }

    val filterByPersonId = Compiled { (personId: Rep[Long]) =>
      Tables.Dote.filter(row => row.personId === personId)
    }

    val recentDotesWithDotable = Compiled {
      (offset: ConstColumn[Long], limit: ConstColumn[Long]) =>
        (for {
          dote <- Tables.Dote
            .sortBy(_.doteTime.desc)
            .drop(offset)
            .take(limit)
          person <- Tables.Person if dote.personId === person.id
          (d, p) <- Tables.Dotable joinLeft Tables.Dotable on (_.parentId === _.id)
          if d.id === dote.dotableId
        } yield (dote, person, d, p)).sortBy(_._1.doteTime.desc)
    }

    val recentDotesWithDotableByPerson = Compiled {
      (offset: ConstColumn[Long], limit: ConstColumn[Long], personId: Rep[Long]) =>
        (for {
          dote <- Tables.Dote
            .filter(_.personId === personId)
            .sortBy(_.doteTime.desc)
            .drop(offset)
            .take(limit)
          person <- Tables.Person if dote.personId === person.id
          (d, p) <- Tables.Dotable joinLeft Tables.Dotable on (_.parentId === _.id)
          if d.id === dote.dotableId
        } yield (dote, person, d, p)).sortBy(_._1.doteTime.desc)
    }

    val recentDotesWithDotableFromFollowing = Compiled {
      (offset: ConstColumn[Long], limit: ConstColumn[Long], personId: Rep[Long]) =>
        val followingPersonIds =
          for {
            follower <- Tables.Follower if follower.followerId === personId
            person <- Tables.Person if person.id === follower.followeeId
          } yield person.id

        (for {
          dote <- Tables.Dote
            .filter(_.personId in followingPersonIds)
            .sortBy(_.doteTime.desc)
            .drop(offset)
            .take(limit)
          person <- Tables.Person if dote.personId === person.id
          (d, p) <- Tables.Dotable joinLeft Tables.Dotable on (_.parentId === _.id)
          if d.id === dote.dotableId
        } yield (dote, person, d, p)).sortBy(_._1.doteTime.desc)
    }

    val mostPopularDotables = Compiled {
      (kind: Rep[DotableKind], maxDoteAge: Rep[LocalDateTime], limit: ConstColumn[Long]) =>
        val dotesForKind = (for {
          dotables <- Tables.Dotable.filter(_.kind === kind)
          dotes <- Tables.Dote if dotes.dotableId === dotables.id && dotes.doteTime >= maxDoteAge
        } yield (dotes, dotables)).groupBy(_._2) map {
          case (dotable, groupedQuery) =>
            val doteCount: Rep[Int] = groupedQuery.length
            (dotable, doteCount)
        }
        dotesForKind.sortBy(_._2.desc).take(limit).map(_._1)
    }

  }

  def readBatchForPerson(personId: Long, dotableIds: Seq[Long]) = {
    // this query can't be compiled because inSet doesn't support compiled queries
    val query =
      Tables.Dote.filter(row => row.personId === personId && row.dotableId.inSet(dotableIds))
    query.result
  }

  def mostPopularDotables(kind: DotableKind, maxDoteAge: LocalDateTime, limit: Long) = {
    Queries.mostPopularDotables(kind, maxDoteAge, limit).result
  }

  def recentRecentDotesWithDotables(paginationInfo: PaginationInfo) = {
    Queries.recentDotesWithDotable(paginationInfo.offset, paginationInfo.pageSize).result
  }

  def recentDotesWithDotableByPerson(paginationInfo: PaginationInfo, personId: Long) = {
    Queries
      .recentDotesWithDotableByPerson(paginationInfo.offset, paginationInfo.pageSize, personId)
      .result
  }

  def recentDotesWithDotableFromFollowing(paginationInfo: PaginationInfo, personId: Long) = {
    Queries
      .recentDotesWithDotableFromFollowing(paginationInfo.offset,
                                           paginationInfo.pageSize,
                                           personId)
      .result
  }

  def readDote(personId: Long, dotableId: Long) = {
    Queries.filterByPersonAndDotableId(personId, dotableId).result.headOption
  }

  def upsert(personId: Long, dotableId: Long, dote: Dote) = {
    val emoteKind: Option[EmoteKinds.Value] = EmoteKindMapper.fromProto(dote.emoteKind)
    sqlu"""INSERT INTO dote AS d
           (person_id, dotable_id, emote_kind, dote_time)
           VALUES
           ($personId, $dotableId, $emoteKind, now())
           ON CONFLICT (person_id, dotable_id)
           DO UPDATE SET emote_kind = $emoteKind,
                         dote_time = now()
           WHERE d.person_id = $personId AND d.dotable_id = $dotableId"""
  }

}
