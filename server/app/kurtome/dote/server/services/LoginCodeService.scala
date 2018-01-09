package kurtome.dote.server.services

import java.time.{Duration, LocalDateTime}
import javax.inject._

import dote.proto.api.person.Person
import kurtome.dote.server.util.RandomString
import kurtome.dote.slick.db.gen.Tables

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginCodeService @Inject()(
    personDbService: PersonService,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext) {

  private val codeLength = 20
  private val codeDuration = Duration.ofHours(1)

  def writeNewCode(person: Tables.PersonRow): Future[String] = {
    val code = genCode()
    val expiration = LocalDateTime.now().plus(codeDuration)
    personDbService.writeLoginCode(person.id, code, expiration) map { _ =>
      code
    }
  }

  def attemptLogin(username: String, email: String, code: String): Future[Option[String]] = {
    personDbService.readByUsernameAndEmail(username, email) flatMap {
      case Some(person) => {
        if (person.loginCode.getOrElse("") == code
            && person.loginCodeExpirationTime.isAfter(LocalDateTime.now())) {
          authTokenService.createTokenForRememberMeCookie(person).map(Some(_))
        } else {
          Future(None)
        }
      }
      case None => Future(None)
    }
  }

  private def genCode(): String = {
    RandomString.uppercaseAlphanumeric(codeLength)
  }

}
