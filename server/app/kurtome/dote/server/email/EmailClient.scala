package kurtome.dote.server.email

import javax.inject._

import com.sendgrid._
import dote.proto.api.person.Person
import kurtome.dote.slick.db.gen.Tables
import play.api.Configuration
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailClient @Inject()(configuration: Configuration)(implicit ec: ExecutionContext)
    extends LogSupport {

  private val sg = new SendGrid(configuration.get[String]("kurtome.dote.email.sendgrid.apikey"))

  def send(message: PendingMessage): Future[Response] = {
    val from = new Email("noreply@resonator.fm")
    val subject = message.subject
    val to = new Email(message.person.email)
    val content = new Content("text/plain", message.content)
    val mail = new Mail(from, subject, to, content)

    val request = new Request()
    request.setMethod(Method.POST)
    request.setEndpoint("mail/send")
    request.setBody(mail.build)

    Future {
      val response = sg.api(request)
      if (response.getStatusCode < 200 || response.getStatusCode >= 300) {
        warn(s"Error response from sendgrid [${response.getStatusCode}]: ${response.getBody}")
      }
      response
    }
  }

}

case class PendingMessage(person: Tables.PersonRow, subject: String, content: String)
