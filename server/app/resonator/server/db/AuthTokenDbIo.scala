package resonator.server.db

import java.time.LocalDateTime
import javax.inject._
import javax.sql.rowset.serial.SerialBlob

import resonator.proto.api.person.Person
import resonator.slick.db.DotePostgresProfile.api._
import resonator.slick.db.gen.Tables
import slick.lifted.Compiled

import scala.concurrent.ExecutionContext

@Singleton
class AuthTokenDbIo @Inject()(implicit executionContext: ExecutionContext) {
  object Queries {
    val filterBySelector = Compiled { (selector: Rep[String]) =>
      Tables.AuthToken.filter(_.selector === selector)
    }
  }

  def readBySelector(selector: String) = {
    Queries.filterBySelector(selector).result.headOption
  }

  def insert(selector: String,
             validator: Array[Byte],
             personId: Long,
             expirationTime: LocalDateTime) = {
    Tables.AuthToken += Tables.AuthTokenRow(0,
                                            selector,
                                            validator,
                                            expirationTime,
                                            personId,
                                            dbUpdatedTime = LocalDateTime.MIN)
  }

}
