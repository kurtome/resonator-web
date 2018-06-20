package resonator.server.controllers.api

import javax.inject._

import resonator.proto.action.hello._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HelloController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends ProtobufController[HelloRequest, HelloResponse](cc) {
  override def parseRequest(bytes: Array[Byte]) = HelloRequest.parseFrom(bytes)

  override def action(request: Request[HelloRequest]) = {
    val name = request.body.name
    Future(HelloResponse(s"Frankenstein is my name, $name."))
  }
}
