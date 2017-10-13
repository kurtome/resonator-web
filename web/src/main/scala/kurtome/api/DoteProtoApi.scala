package kurtome.api

import dote.proto.action.addpodcast._
import dote.proto.action.hello._
import kurtome.api.AjaxApiHelper.ProtoAction

import scala.concurrent.Future

object DoteProtoApi {

  def requestAsJson = AjaxApiHelper.jsonRequest(_, _)

  def hello(request: HelloRequest): Future[HelloResponse] =
    AjaxApiHelper.protoRequest(new ProtoAction[HelloRequest, HelloResponse] {
      override val route = "hello"

      override def serializeRequest(r: HelloRequest) =
        HelloRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = HelloResponse.parseFrom(r)
    })(request)

  def addPodcast(request: AddPodcastRequest) =
    AjaxApiHelper.protoRequest(new ProtoAction[AddPodcastRequest, AddPodcastResponse] {
      override val route = "add-podcast"

      override def serializeRequest(r: AddPodcastRequest) =
        AddPodcastRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) =
        AddPodcastResponse.parseFrom(r)
    })(request)

}
