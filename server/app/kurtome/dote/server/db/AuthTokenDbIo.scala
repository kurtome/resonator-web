package kurtome.dote.server.db

import java.time.LocalDateTime
import javax.inject._
import javax.sql.rowset.serial.SerialBlob

import kurtome.dote.proto.api.person.Person
import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.gen.Tables
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
    Tables.AuthToken += Tables.AuthTokenRow(0, selector, validator, expirationTime, personId)
  }

}
