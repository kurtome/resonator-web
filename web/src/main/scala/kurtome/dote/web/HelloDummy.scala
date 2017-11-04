package kurtome.dote.web

import dote.proto.action.hello.HelloRequest
import kurtome.dote.web.rpc.DoteProtoServer

import scala.scalajs.js.Dynamic
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object HelloDummy {

  def runApiRequests: Unit = {
    println("Preparing to run API requests...")

    val request = HelloRequest(name = "kmel")

    // Test bytes request
    println("Running proto bytes request...")
    DoteProtoServer.hello(request) onComplete {
      case Success(response) =>
        println("Got proto response: " + response.message)
      case Failure(ex) => println("Proto failure: " + ex.getMessage)
    }

    // Test JSON request
    println("Running proto JSON request...")
    DoteProtoServer.requestAsJson("hello", Dynamic.literal("name" -> "Jason")) onComplete {
      case Success(response) =>
        println("Got JSON response: " + response.message)
      case Failure(ex) => println("JSON failure: " + ex.getMessage)
    }

  }
}
