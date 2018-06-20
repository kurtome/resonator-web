package resonator.web.rpc

import resonator.proto.api.action.add_podcast._
import resonator.proto.action.hello._
import resonator.proto.api.action.login_link._
import resonator.proto.api.action.get_logged_in_person._
import resonator.proto.api.action.get_dotable._
import resonator.proto.api.action.get_dotable_list._
import resonator.proto.api.action.get_feed._
import resonator.proto.api.action.get_follower_summary.GetFollowerSummaryRequest
import resonator.proto.api.action.get_follower_summary.GetFollowerSummaryResponse
import resonator.proto.api.action.get_radio_schedule.GetRadioScheduleRequest
import resonator.proto.api.action.get_radio_schedule.GetRadioScheduleResponse
import resonator.proto.api.action.get_radio_station_details._
import resonator.proto.api.action.update_radio_station_details._
import resonator.proto.api.action.search._
import resonator.proto.api.action.set_dote._
import resonator.proto.api.action.set_follow._
import resonator.web.rpc.AjaxRpc.ProtoAction
import resonator.web.utils.PerfTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ResonatorApiClient {

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
      response
    }

  def getFeed(request: GetFeedRequest) =
    AjaxRpc.protoRequest(new ProtoAction[GetFeedRequest, GetFeedResponse] {
      override val route = "get-feed"

      override def serializeRequest(r: GetFeedRequest) =
        GetFeedRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = GetFeedResponse.parseFrom(r)
    })(request) map { response =>
      response
    }

  def search(request: SearchRequest) =
    AjaxRpc.protoRequest(new ProtoAction[SearchRequest, SearchResponse] {
      override val route = "search"

      override def serializeRequest(r: SearchRequest) =
        SearchRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = SearchResponse.parseFrom(r)
    })(request) map { response =>
      response
    }

  def loginLink(request: LoginLinkRequest) =
    AjaxRpc.protoRequest(new ProtoAction[LoginLinkRequest, LoginLinkResponse] {
      override val route = "create-login-link"

      override def serializeRequest(r: LoginLinkRequest) =
        LoginLinkRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = LoginLinkResponse.parseFrom(r)
    })(request)

  def setDote(request: SetDoteRequest) =
    AjaxRpc.protoRequest(new ProtoAction[SetDoteRequest, SetDoteResponse] {
      override val route = "set-dote"

      override def serializeRequest(r: SetDoteRequest) =
        SetDoteRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = SetDoteResponse.parseFrom(r)
    })(request)

  def getFollowerSummary(request: GetFollowerSummaryRequest) =
    AjaxRpc.protoRequest(new ProtoAction[GetFollowerSummaryRequest, GetFollowerSummaryResponse] {
      override val route = "get-follower-summary"

      override def serializeRequest(r: GetFollowerSummaryRequest) =
        GetFollowerSummaryRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = GetFollowerSummaryResponse.parseFrom(r)
    })(request)

  def setFollow(request: SetFollowRequest) =
    AjaxRpc.protoRequest(new ProtoAction[SetFollowRequest, SetFollowResponse] {
      override val route = "set-follow"

      override def serializeRequest(r: SetFollowRequest) =
        SetFollowRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = SetFollowResponse.parseFrom(r)
    })(request)

  def getRadioSchedule(request: GetRadioScheduleRequest) =
    AjaxRpc.protoRequest(new ProtoAction[GetRadioScheduleRequest, GetRadioScheduleResponse] {
      override val route = "get-radio-schedule"

      override def serializeRequest(r: GetRadioScheduleRequest) =
        GetRadioScheduleRequest.toByteArray(r)

      override def parseResponse(r: Array[Byte]) = GetRadioScheduleResponse.parseFrom(r)
    })(request)

  def getRadioStationDetails(request: GetRadioStationDetailsRequest) =
    AjaxRpc.protoRequest(
      new ProtoAction[GetRadioStationDetailsRequest, GetRadioStationDetailsResponse] {
        override val route = "get-radio-station-details"

        override def serializeRequest(r: GetRadioStationDetailsRequest) =
          GetRadioStationDetailsRequest.toByteArray(r)

        override def parseResponse(r: Array[Byte]) = GetRadioStationDetailsResponse.parseFrom(r)
      })(request)

  def updateRadioStation(request: UpdateRadioStationDetailsRequest) =
    AjaxRpc.protoRequest(
      new ProtoAction[UpdateRadioStationDetailsRequest, UpdateRadioStationDetailsResponse] {
        override val route = "update-radio-station"

        override def serializeRequest(r: UpdateRadioStationDetailsRequest) =
          UpdateRadioStationDetailsRequest.toByteArray(r)

        override def parseResponse(r: Array[Byte]) = UpdateRadioStationDetailsResponse.parseFrom(r)
      })(request)
}
