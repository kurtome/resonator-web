package kurtome.dote.web.rpc

import java.nio.ByteBuffer

import org.scalajs.dom
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.{Dynamic, JSON}
import scala.scalajs.js.typedarray._

private[rpc] object AjaxRpc {

  trait ProtoAction[TRequest, TResponse] {
    val route: String
    def serializeRequest(request: TRequest): Array[Byte]
    def parseResponse(response: Array[Byte]): TResponse
  }

  val baseUrl: String = dom.window.location.protocol + "//" + dom.window.location.host + "/api/v1/"

  val csrfToken: String = dom.document
    .getElementById("csrf-token-holder")
    .getAttribute("x-csrf-value")

  def protoRequest[TRequest, TResponse](action: ProtoAction[TRequest, TResponse])(
      request: TRequest): Future[TResponse] = {
    println("requesting from " + baseUrl + action.route)
    val url = baseUrl + action.route
    Ajax.post(
      url = baseUrl + action.route,
      data = Ajax.InputData.byteBuffer2ajax(ByteBuffer.wrap(action.serializeRequest(request))),
      responseType = "arraybuffer",
      headers = Map(
        "Content-Type" -> "application/x-protobuf",
        "Csrf-Token" -> csrfToken
      )
    ) map { xhr =>
      {
        val dataArray = xhr.response.asInstanceOf[Int8Array]
        val array = int8Array2ByteArray(dataArray)
        action.parseResponse(array)
      }
    }

  }

  def jsonRequest(route: String, json: Dynamic): Future[Dynamic] = {
    Ajax.post(
      url = baseUrl + route,
      data = Ajax.InputData.str2ajax(JSON.stringify(json)),
      headers = Map(
        "Content-Type" -> "application/json",
        "Csrf-Token" -> csrfToken
      )
    ) map { xhr =>
      JSON.parse(xhr.responseText)
    }
  }

}
