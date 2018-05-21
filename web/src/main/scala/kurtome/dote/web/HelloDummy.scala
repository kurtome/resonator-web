package kurtome.dote.web

import kurtome.dote.proto.action.hello.HelloRequest
import kurtome.dote.web.rpc.ResonatorApiClient
import wvlet.log.LogSupport

import scala.scalajs.js.Dynamic
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object HelloDummy extends LogSupport {

  def runApiRequests: Unit = {
    info("Preparing to run API requests...")

    val request = HelloRequest(name = "kmel")

    // Test bytes request
    info("Running proto bytes request...")
    ResonatorApiClient.hello(request) onComplete {
      case Success(response) =>
        info("Got proto response: " + response.message)
      case Failure(ex) => warn("Proto failure: " + ex.getMessage)
    }

    // Test JSON request
    info("Running proto JSON request...")
    ResonatorApiClient.requestAsJson("hello", Dynamic.literal("name" -> "Jason")) onComplete {
      case Success(response) =>
        info("Got JSON response: " + response.message)
      case Failure(ex) => warn("JSON failure: " + ex.getMessage)
    }

  }
}
