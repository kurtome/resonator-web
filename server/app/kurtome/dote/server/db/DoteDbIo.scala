package kurtome.dote.server.db

import java.time.LocalDateTime

import javax.inject._
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.slick.db.DotableKinds.DotableKind
import kurtome.dote.slick.db.DotePostgresProfile.api._
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

    val recentDotesWithDotable = Compiled { (limit: ConstColumn[Long]) =>
      (for {
        dote <- Tables.Dote
          .filter(row =>
            row.smileCount > 0 || row.cryCount > 0 || row.laughCount > 0 || row.scowlCount > 0)
          .sortBy(_.doteTime.desc)
          .take(limit)
        person <- Tables.Person if dote.personId === person.id
        (d, p) <- Tables.Dotable joinLeft Tables.Dotable on (_.parentId === _.id)
        if d.id === dote.dotableId
      } yield (dote, person, d, p)).sortBy(_._1.doteTime.desc)
    }

    val mostPopularDotables = Compiled {
      (kind: Rep[DotableKind], maxDoteAge: Rep[LocalDateTime], limit: ConstColumn[Long]) =>
        val dotesForKind = for {
          dotables <- Tables.Dotable.filter(_.kind === kind)
          dotes <- Tables.Dote if dotes.dotableId === dotables.id && dotes.doteTime >= maxDoteAge
        } yield (dotes.scowlCount + dotes.laughCount + dotes.smileCount + dotes.cryCount, dotables)
        dotesForKind.sortBy(_._1.desc).take(limit).map(_._2)
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

  def recentRecentDotesWithDotables(limit: Long) = {
    Queries.recentDotesWithDotable(limit).result
  }

  def readDote(personId: Long, dotableId: Long) = {
    Queries.filterByPersonAndDotableId(personId, dotableId).result.headOption
  }

  def upsert(personId: Long, dotableId: Long, dote: Dote) = {
    sqlu"""INSERT INTO dote AS d
           (person_id, dotable_id, smile_count, cry_count, laugh_count, scowl_count, dote_time)
           VALUES
           ($personId, $dotableId, ${dote.smileCount}, ${dote.cryCount}, ${dote.laughCount}, ${dote.scowlCount}, now())
           ON CONFLICT (person_id, dotable_id)
           DO UPDATE SET smile_count = ${dote.smileCount},
                         cry_count = ${dote.cryCount},
                         laugh_count = ${dote.laughCount},
                         scowl_count = ${dote.scowlCount},
                         dote_time = now()
           WHERE d.person_id = $personId AND d.dotable_id = $dotableId"""
  }

}
