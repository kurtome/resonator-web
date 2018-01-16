package kurtome.dote.server.services

import java.net.URLEncoder
import java.time.{Duration, LocalDateTime}
import javax.inject._

import kurtome.dote.proto.api.person.Person
import kurtome.dote.server.email.{EmailClient, PendingMessage}
import kurtome.dote.server.util.RandomString
import kurtome.dote.slick.db.gen.Tables

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginCodeService @Inject()(
    emailClient: EmailClient,
    personDbService: PersonService,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext) {

  private val codeLength = 20
  private val codeDuration = Duration.ofHours(1)

  /**
    * Creates a login code and sends an email to the person unless they already have a recent login
    * code, in which case the existing code won't be changed (so that the already sent link will
    * remain valid) and no new email will be sent.
    *
    * @param personEmail must be an existing person with this email
    * @param linkPrefix the scheme and domain to use for the link
    * @return future that will complete when operations are complete
    */
  def createCodeAndSendLoginEmail(personEmail: String, linkPrefix: String): Future[Unit] = {
    personDbService.readByEmail(personEmail) flatMap {
      case Some(person) =>
        val newCodeCutoff =
          LocalDateTime.now().plus(codeDuration).minus(Duration.ofMinutes(10))
        if (person.loginCodeExpirationTime.isAfter(newCodeCutoff)) {
          // code is less than 10 minutes old, don't re-send email
          Future(Unit)
        } else {
          writeNewCodeToPerson(person) flatMap { code =>
            val emailEncoded = URLEncoder.encode(person.email, "UTF-8")
            val loginLink = s"$linkPrefix/lc/$emailEncoded/$code"
            emailClient
              .send(PendingMessage(
                person,
                "Login to Resonator",
                s"Welcome, your username is ${person.username}, please login with this link: $loginLink"))
              .map(_ => Unit)
          }
        }
      case _ => throw new RuntimeException("Unexpected, person should exist.")
    }
  }

  def writeNewCodeToPerson(person: Tables.PersonRow): Future[String] = {
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
