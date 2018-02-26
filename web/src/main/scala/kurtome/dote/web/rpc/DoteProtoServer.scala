package kurtome.dote.web.rpc

import kurtome.dote.proto.api.action.add_podcast._
import kurtome.dote.proto.action.hello._
import kurtome.dote.proto.api.action.login_link._
import kurtome.dote.proto.api.action.get_logged_in_person._
import kurtome.dote.proto.api.action.get_dotable._
import kurtome.dote.proto.api.action.get_dotable_list._
import kurtome.dote.proto.api.action.get_feed._
import kurtome.dote.proto.api.action.search._
import kurtome.dote.proto.api.action.set_dote._
import kurtome.dote.proto.api.action.set_follow._
import kurtome.dote.web.rpc.AjaxRpc.ProtoAction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DoteProtoServer {

  def requestAsJson = AjaxRpc.jsonRequest _

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

  def getDotableDetails(request: GetDotableDetailsRequest) =
    AjaxRpc.protoRequest(new ProtoAction[GetDotableDetailsRequest, GetDotableDetailsResponse] {
      override val route = "get-dotable-details"

      override def serializeRequest(r: GetDotableDetailsRequest) =
        GetDotableDetailsRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = GetDotableDetailsResponse.parseFrom(r)
    })(request) map { response =>
      response.dotable foreach { dotable =>
        LocalCacheWorkerManager.put(LocalCache.ObjectKinds.DotableDetails, dotable.id, dotable)
      }
      response
    }

  def getDotableList(request: GetDotableListRequest) =
    AjaxRpc.protoRequest(new ProtoAction[GetDotableListRequest, GetDotableListResponse] {
      override val route = "get-dotable-list"

      override def serializeRequest(r: GetDotableListRequest) =
        GetDotableListRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = GetDotableListResponse.parseFrom(r)
    })(request) map { response =>
      response.dotables.foreach(d =>
        LocalCacheWorkerManager.put(LocalCache.ObjectKinds.DotableShallow, d.id, d))
      response
    }

  def getFeed(request: GetFeedRequest) =
    AjaxRpc.protoRequest(new ProtoAction[GetFeedRequest, GetFeedResponse] {
      override val route = "get-feed"

      override def serializeRequest(r: GetFeedRequest) =
        GetFeedRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = GetFeedResponse.parseFrom(r)
    })(request) map { response =>
      LocalCacheWorkerManager.put(LocalCache.ObjectKinds.Feed,
                                  response.getFeed.getId.toString,
                                  response.getFeed)
      val lists = response.getFeed.items.map(_.getDotableList.getList)
      lists
        .flatMap(_.dotables)
        .foreach(d => LocalCacheWorkerManager.put(LocalCache.ObjectKinds.DotableShallow, d.id, d))
      response
    }

  def search(request: SearchRequest) =
    AjaxRpc.protoRequest(new ProtoAction[SearchRequest, SearchResponse] {
      override val route = "search"

      override def serializeRequest(r: SearchRequest) =
        SearchRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = SearchResponse.parseFrom(r)
    })(request) map { response =>
      response.dotables.foreach(d =>
        LocalCacheWorkerManager.put(LocalCache.ObjectKinds.DotableShallow, d.id, d))
      response
    }

  def loginLink(request: LoginLinkRequest) =
    AjaxRpc.protoRequest(new ProtoAction[LoginLinkRequest, LoginLinkResponse] {
      override val route = "create-login-link"

      override def serializeRequest(r: LoginLinkRequest) =
        LoginLinkRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = LoginLinkResponse.parseFrom(r)
    })(request)

  def getLoggedInPerson(request: GetLoggedInPersonRequest) =
    AjaxRpc.protoRequest(new ProtoAction[GetLoggedInPersonRequest, GetLoggedInPersonResponse] {
      override val route = "get-logged-in-person"

      override def serializeRequest(r: GetLoggedInPersonRequest) =
        GetLoggedInPersonRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = GetLoggedInPersonResponse.parseFrom(r)
    })(request)

  def setDote(request: SetDoteRequest) =
    AjaxRpc.protoRequest(new ProtoAction[SetDoteRequest, SetDoteResponse] {
      override val route = "set-dote"

      override def serializeRequest(r: SetDoteRequest) =
        SetDoteRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = SetDoteResponse.parseFrom(r)
    })(request)

  def setFollow(request: SetFollowRequest) =
    AjaxRpc.protoRequest(new ProtoAction[SetFollowRequest, SetFollowResponse] {
      override val route = "set-follow"

      override def serializeRequest(r: SetFollowRequest) =
        SetFollowRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = SetFollowResponse.parseFrom(r)
    })(request)
}
