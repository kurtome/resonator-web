package controllers.api

import javax.inject._

import controllers.podcast.PodcastFeedFetcher
import dote.proto.action.addpodcast._
import dote.proto.model.doteentity._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AddPodcastController @Inject()(cc: ControllerComponents, podcastFetcher: PodcastFeedFetcher)
    extends ProtobufController[AddPodcastRequest, AddPodcastResponse](cc) {
  override def parseRequest(bytes: Array[Byte]) =
    AddPodcastRequest.parseFrom(bytes)

  override def action(request: AddPodcastRequest) = {
    val eventualPodcast: Future[DoteEntity] =
      podcastFetcher.fetch(request.feedUrl)

    eventualPodcast map { e =>
      AddPodcastResponse(Option(e))
    }
  }
}
