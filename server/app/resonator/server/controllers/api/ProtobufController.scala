package resonator.server.controllers.api

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.util.ByteString
import com.trueaccord.scalapb.json.JsonFormat
import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
import play.api.libs.streams.Accumulator
import play.api.mvc._
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

abstract class ProtobufController[
    TRequest <: GeneratedMessage with Message[TRequest]: GeneratedMessageCompanion,
    TResponse <: GeneratedMessage with Message[TResponse]: GeneratedMessageCompanion](
    cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with LogSupport {

  type ActionRequest = TRequest
  type ActionResponse = TResponse

  def parseRequest(bytes: Array[Byte]): TRequest

  def action(request: Request[TRequest]): Future[ProtoResponse[TResponse]]

  def parseJsonRequest(json: String): TRequest =
    JsonFormat.fromJsonString[TRequest](json)

  implicit def response2Wrapper(response: TResponse): ProtoResponse[TResponse] = {
    ProtoResponse(response, identity)
  }

  implicit def response2Wrapper(response: Future[TResponse]): Future[ProtoResponse[TResponse]] = {
    response.map(ProtoResponse(_, identity))
  }

  def protoAction() = Action.async(new ProtoParser) { implicit request: Request[TRequest] =>
    debug(s"handling request: ${request.body}")
    action(request) map {
      case ProtoResponse(response, okFilter) =>
        // See if request accepts gzip responses, web browsers will automatically inflate from gzip
        // when the Content-Encoding header is set in the response below
        val acceptsGzip = request.headers.get("Accept-Encoding").exists { header =>
          header.split(Array(' ', ',', ';')).contains("gzip")
        }

        request.contentType map {
          case "application/x-protobuf" => {
            if (acceptsGzip) {
              okFilter(Ok(gzip(response.toByteArray)).withHeaders("Content-Encoding" -> "gzip"))
            } else {
              okFilter(Ok(response.toByteArray))
            }
          }
          case _ => {
            if (acceptsGzip) {
              okFilter(
                Ok(gzip(JsonFormat.toJsonString(response).getBytes))
                  .withHeaders("Content-Encoding" -> "gzip"))
            } else {
              okFilter(Ok(JsonFormat.toJsonString(response)))
            }
          }
        } get
    }
  }

  private def gzip(bytes: Array[Byte]): Array[Byte] = {
    val byteSteam = new ByteArrayOutputStream(bytes.size)
    val gzipStream = new GZIPOutputStream(byteSteam)
    try {
      gzipStream.write(bytes)
      gzipStream.close()
      val zippedBytes = byteSteam.toByteArray
      zippedBytes
    } finally {
      gzipStream.close()
      byteSteam.close()
    }
  }

  private class ProtoParser extends BodyParser[TRequest] with Results {
    val jsonSink: Sink[ByteString, Future[Either[Result, TRequest]]] =
      Flow[ByteString]
        .map { bytes =>
          Try {
            val s = bytes.decodeString("UTF-8")
            parseJsonRequest(s)
          }.toEither.left.map(t => BadRequest("Unable to parse JSON."))
        }
        .toMat(Sink.head)(Keep.right)

    val bytesSink: Sink[ByteString, Future[Either[Result, TRequest]]] =
      Flow[ByteString]
        .map { bytes =>
          Try(parseRequest(bytes.toArray)).toEither.left.map(t =>
            BadRequest("Unable to parse bytes."))
        }
        .toMat(Sink.head)(Keep.right)

    override def apply(header: RequestHeader): Accumulator[ByteString, Either[Result, TRequest]] = {
      header.contentType map {
        case "application/x-protobuf" => Accumulator(bytesSink)
        case "application/json" => Accumulator(jsonSink)
        case _ =>
          Accumulator.done(Left(BadRequest("API only supports protobuf as JSON or bytes.")))
      } get
    }
  }
}
case class ProtoResponse[TResponse](response: TResponse, okFilter: (Result) => Result = identity)
