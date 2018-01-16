package kurtome.dote.server.db

import java.time.LocalDateTime
import javax.inject._

import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.gen.Tables
import slick.lifted.Compiled

import scala.concurrent.ExecutionContext

@Singleton
class DoteDbIo @Inject()(implicit executionContext: ExecutionContext) {
  object Queries {
    val filterByPersonAndDotableId = Compiled { (personId: Rep[Long], dotableId: Rep[Long]) =>
      Tables.Dote.filter(row => row.personId === personId && row.dotableId === dotableId)
    }

    val filterByPersonId = Compiled { (personId: Rep[Long]) =>
      Tables.Dote.filter(row => row.personId === personId)
    }

  }

  def readBatchForPerson(personId: Long, dotableIds: Seq[Long]) = {
    // this query can't be compiled because inSet doesn't support compiled queries
    val query =
      Tables.Dote.filter(row => row.personId === personId && row.dotableId.inSet(dotableIds))
    query.result
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
