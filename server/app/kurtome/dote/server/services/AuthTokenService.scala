package kurtome.dote.server.services

import java.security.MessageDigest
import java.time.{Duration, LocalDateTime}
import javax.inject._

import kurtome.dote.server.db.AuthTokenDbIo
import kurtome.dote.server.util.RandomString
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.web.shared.util.result._
import play.api.Configuration
import slick.basic.BasicBackend
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthTokenService @Inject()(db: BasicBackend#Database,
                                 personService: PersonService,
                                 config: Configuration,
                                 authTokenDbIo: AuthTokenDbIo)(implicit ec: ExecutionContext)
    extends LogSupport {

  private val selectorLength = 32
  private val validatorLength = 32
  private val salt = config.get[String]("kurtome.dote.auth.token.salt")
  private val sha256 = MessageDigest.getInstance("SHA-256")

  def readPersonForCookieToken(
      cookieToken: String): Future[ProduceAction[Option[Tables.PersonRow]]] = {
    if (cookieToken.length != (selectorLength + validatorLength)) {
      None
    }
    val selector = cookieToken.substring(0, 32)
    val validator = cookieToken.substring(32)
    assert(selector.length == 32)
    assert(validator.length == 32)
    db.run(authTokenDbIo.readBySelector(selector)) flatMap {
      case Some(authToken) => {
        val incomingDbValidator = calcDbValidator(validator, authToken.personId)
        val existingDbValidator = authToken.validator
        if (incomingDbValidator sameElements existingDbValidator) {
          personService.readById(authToken.personId).map(SuccessData(_))
        } else {
          Future(FailedData(None, StatusCodes.NotLoggedIn))
        }
      }
      case None => {
        Future(FailedData(None, StatusCodes.NotLoggedIn))
      }
    }
  }

  def createTokenForRememberMeCookie(person: Tables.PersonRow): Future[String] = {
    val tokenParts = generateTokenParts(person.id)
    val expiration = LocalDateTime.now().plus(AuthTokenService.tokenDuration)
    db.run(
      authTokenDbIo.insert(selector = tokenParts.selector,
                           validator = tokenParts.dbValidator,
                           personId = person.id,
                           expirationTime = expiration)) map { _ =>
      tokenParts.cookieToken
    }
  }

  private def generateTokenParts(personId: Long): TokenParts = {
    val selector = RandomString.fullAlphanumeric(selectorLength)

    val rawValidator = RandomString.fullAlphanumeric(validatorLength)

    val cookieToken = selector + rawValidator

    val dbValidator = calcDbValidator(rawValidator, personId)

    TokenParts(selector, dbValidator, cookieToken)
  }

  private def calcDbValidator(validator: String, personId: Long): Array[Byte] = {
    val combinedValidator = validator + salt + personId
    sha256.digest(combinedValidator.getBytes)
  }

}
object AuthTokenService {
  val tokenDuration = Duration.ofDays(60)
}

case class TokenParts(selector: String, dbValidator: Array[Byte], cookieToken: String)
