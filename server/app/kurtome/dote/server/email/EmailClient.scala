package kurtome.dote.server.email

import javax.inject._
import kurtome.dote.slick.db.gen.Tables
import org.matthicks.mailgun._
import play.api.Configuration
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailClient @Inject()(configuration: Configuration)(implicit ec: ExecutionContext)
    extends LogSupport {

  private val mailgunDomain = configuration.get[String]("kurtome.dote.email.mailgun.domain")
  private val mailgunApiKey = configuration.get[String]("kurtome.dote.email.mailgun.apikey")

  private val mailgun = new Mailgun(mailgunDomain, mailgunApiKey)

  def send(message: PendingMessage): Future[Unit] = {
    debug(s"sending ${message.subject}")
    val pendingResponse = mailgun.send(
      Message.simple(
        from = EmailAddress("noreply@resonator.fm", "Resonator"),
        to = EmailAddress(message.person.email),
        subject = message.subject,
        text = message.content
      )) recover {
      case t => warn("Email send failed.", t)
    }

    pendingResponse map { response =>
      debug(response)
      Unit
    }
  }

}

case class PendingMessage(person: Tables.PersonRow, subject: String, content: String)
