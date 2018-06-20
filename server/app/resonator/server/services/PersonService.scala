package resonator.server.services

import java.time.LocalDateTime
import javax.inject._

import resonator.server.db.PersonDbIo
import resonator.shared.util.result
import resonator.shared.util.result._
import resonator.slick.db.gen.Tables
import slick.basic.BasicBackend
import wvlet.log.LogSupport

import scala.concurrent._
import scala.util._

@Singleton
class PersonService @Inject()(db: BasicBackend#Database, personDbIo: PersonDbIo)(
    implicit ec: ExecutionContext)
    extends LogSupport {

  def writeLoginCode(id: Long, code: String, expiration: LocalDateTime): Future[Unit] = {
    db.run(
        personDbIo
          .writeLoginCode(id, code, expiration))
      .map(_ => Unit)
  }

  def readById(id: Long): Future[Option[Tables.PersonRow]] = {
    db.run(personDbIo.readById(id))
  }

  def readByUsernameAndEmail(username: String, email: String): Future[Option[Tables.PersonRow]] = {
    db.run(personDbIo.readByUsernameAndEmail(username, email))
  }

  def readByUsername(username: String): Future[Option[Tables.PersonRow]] = {
    db.run(personDbIo.readByUsername(username))
  }

  def readByEmail(email: String): Future[Option[Tables.PersonRow]] = {
    db.run(personDbIo.readByEmail(email))
  }

  def createPerson(username: String,
                   email: String): Future[ProduceAction[Option[Tables.PersonRow]]] = {
    db.run(personDbIo.filterByUsernameOrEmail(username, email)) flatMap { existingPeople =>
      val usernameExists: Boolean = existingPeople.exists(_.username == username)
      val emailExists: Boolean = existingPeople.exists(_.email == email)
      if (usernameExists || emailExists) {
        val error = if (usernameExists && emailExists) {
          // Shouldn't happen since there is no reason to call createPerson for an existing person
          result.UnknownErrorStatus
        } else if (usernameExists) {
          ErrorStatus(ErrorCauses.Username, StatusCodes.NotUnique)
        } else {
          ErrorStatus(ErrorCauses.EmailAddress, StatusCodes.NotUnique)
        }
        Future(FailedData(None, error))
      } else {
        Try {
          insertAndGet(username, email) map { insertedPerson =>
            if (insertedPerson.username != username || insertedPerson.email != email) {
              error(s"Read person didn't match inserted person. $username $email $insertedPerson")
              FailedData(None, result.UnknownErrorStatus)
            } else {
              SuccessData(Some(insertedPerson))
            }
          }
        } recover {
          case t: Throwable =>
            // Most likely just a race confition between two parallel inserts for the same username
            info("Error while inserting person.", t)
            Future(FailedData(None, result.UnknownErrorStatus))
        } get
      }
    }
  }

  private def insertAndGet(username: String, email: String): Future[Tables.PersonRow] = {
    val query = for {
      _ <- personDbIo.insertIfNotExists(username, email)
      insertedPerson <- personDbIo.readByUsername(username)
    } yield insertedPerson.get

    db.run(query)
  }

}
