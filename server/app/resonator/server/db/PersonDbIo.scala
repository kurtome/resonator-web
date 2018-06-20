package resonator.server.db

import java.time.LocalDateTime

import resonator.server.util.{Slug, UrlIds}
import resonator.slick.db.gen.Tables
import resonator.slick.db.gen.Tables.PersonRow
import resonator.slick.db.DotePostgresProfile.api._
import javax.inject._

import resonator.proto.api.person.Person
import slick.lifted.Compiled

import scala.concurrent.ExecutionContext

@Singleton
class PersonDbIo @Inject()(implicit executionContext: ExecutionContext) {
  object Queries {
    val filterById = Compiled { (id: Rep[Long]) =>
      Tables.Person.filter(_.id === id)
    }

    val filterByUsername = Compiled { (username: Rep[String]) =>
      Tables.Person.filter(_.username === username)
    }

    val filterByEmailAddress = Compiled { (email: Rep[String]) =>
      Tables.Person.filter(_.email === email)
    }

    val selectForUpdateByEmail = Compiled { (email: Rep[String]) =>
      Tables.Person.filter(_.email === email).forUpdate
    }

    val filterByEmail = Compiled { (email: Rep[String]) =>
      Tables.Person.filter(row => row.email === email)
    }

    val filterByUsernameAndEmail = Compiled { (username: Rep[String], email: Rep[String]) =>
      Tables.Person.filter(row => row.username === username && row.email === email)
    }

    val filterByUsernameOrEmail = Compiled { (username: Rep[String], email: Rep[String]) =>
      Tables.Person.filter(row => row.username === username || row.email === email)
    }

    val readCodeAndExpiration = Compiled { (id: Rep[Long]) =>
      Tables.Person
        .filter(_.id === id)
        .map(row => (row.loginCode, row.loginCodeExpirationTime))
    }
  }

  def readById(id: Long) = {
    Queries.filterById(id).result.headOption
  }

  def writeLoginCode(id: Long, code: String, expiration: LocalDateTime) = {
    Queries.readCodeAndExpiration(id).update((Some(code), expiration))
  }

  def readByEmail(email: String) = {
    Queries.filterByEmail(email).result.headOption
  }

  /**
    * returns a all users which match username or email (max of 2 results)
    */
  def filterByUsernameOrEmail(username: String, email: String) = {
    Queries.filterByUsernameOrEmail(username, email).result
  }

  def readByUsername(username: String) = {
    Queries.filterByUsername(username).result.headOption
  }

  def readByUsernameAndEmail(username: String, email: String) = {
    Queries.filterByUsernameAndEmail(username, email).result.headOption
  }

  def insertIfNotExists(username: String, email: String) = {
    sqlu"""INSERT INTO person (username, email)
         SELECT $username, $email
         WHERE NOT EXISTS (
         SELECT 1 FROM person p
         WHERE p.username = $username AND p.email = $email)"""
  }

}
