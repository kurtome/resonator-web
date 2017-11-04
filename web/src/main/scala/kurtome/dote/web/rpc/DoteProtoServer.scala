package kurtome.dote.web.rpc

import dote.proto.api.action.add_podcast._
import dote.proto.action.hello._
import dote.proto.api.action.get_dotable._
import dote.proto.api.action.get_dotable_list._
import dote.proto.api.dotable.Dotable
import kurtome.dote.web.rpc.AjaxRpc.ProtoAction
import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object DoteProtoServer {

  def requestAsJson = AjaxRpc.jsonRequest(_, _)

  def hello(request: HelloRequest): Future[HelloResponse] =
    AjaxRpc.protoRequest(new ProtoAction[HelloRequest, HelloResponse] {
      override val route = "hello"

      override def serializeRequest(r: HelloRequest) =
        HelloRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = HelloResponse.parseFrom(r)
    })(request)

  def addPodcast(request: AddPodcastRequest) =
    AjaxRpc.protoRequest(new ProtoAction[AddPodcastRequest, AddPodcastResponse] {
      override val route = "add-podcast"

      override def serializeRequest(r: AddPodcastRequest) =
        AddPodcastRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) =
        AddPodcastResponse.parseFrom(r)
    })(request)

  def addGetDotableDetails(request: GetDotableDetailsRequest) =
    AjaxRpc.protoRequest(new ProtoAction[GetDotableDetailsRequest, GetDotableDetailsResponse] {
      override val route = "get-dotable-details"

      override def serializeRequest(r: GetDotableDetailsRequest) =
        GetDotableDetailsRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = GetDotableDetailsResponse.parseFrom(r)
    })(request) map { response =>
      response.dotable map { dotable =>
        LocalCache.put(includesDetails = true, dotable = dotable)
      }
      response
    }

  def addGetDotableList(request: GetDotableListRequest) =
    AjaxRpc.protoRequest(new ProtoAction[GetDotableListRequest, GetDotableListResponse] {
      override val route = "get-dotable-list"

      override def serializeRequest(r: GetDotableListRequest) =
        GetDotableListRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = GetDotableListResponse.parseFrom(r)
    })(request) map { response =>
      response.dotables.foreach(d => LocalCache.put(includesDetails = false, dotable = d))
      response
    }

  private def cache(dotable: Dotable) = {
    val id = dotable.id
    dom.window.localStorage.setItem(s"dotable-$id", "")
  }
}
