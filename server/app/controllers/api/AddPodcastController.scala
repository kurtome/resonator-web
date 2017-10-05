package controllers.api

import javax.inject._

import controllers.podcast.PodcastFeedFetcher
import dote.proto.addpodcast.{AddPodcastRequest, AddPodcastResponse}
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

@Singleton
class AddPodcastController @Inject()(cc: ControllerComponents,
                                     podcastFetcher: PodcastFeedFetcher)
    extends ProtobufController[AddPodcastRequest, AddPodcastResponse](cc) {
  override def parseRequest(bytes: Array[Byte]) =
    AddPodcastRequest.parseFrom(bytes)

  override def action(request: AddPodcastRequest) = {
    val eventualPodcast: Future[podcastFetcher.Podcast] =
      podcastFetcher.fetch(request.feedUrl)
    eventualPodcast map { podcast =>
      val response: AddPodcastResponse =
        AddPodcastResponse(title = podcast.title,
                           shortDescription = podcast.shortDescription)
      response
    }
  }
}
