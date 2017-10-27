package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.server.controllers.podcast.PodcastFeedFetcher
import dote.proto.action.add_podcast._
import dote.proto.model.dote_entity._
import kurtome.dote.server.db.Database
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AddPodcastController @Inject()(cc: ControllerComponents,
                                     podcastFetcher: PodcastFeedFetcher,
                                     database: Database)
    extends ProtobufController[AddPodcastRequest, AddPodcastResponse](cc) {
  override def parseRequest(bytes: Array[Byte]) =
    AddPodcastRequest.parseFrom(bytes)

  override def action(request: AddPodcastRequest) = {
    val eventualPodcast: Future[DoteEntity] =
      podcastFetcher.fetch(request.feedUrl)

    eventualPodcast flatMap { e =>
      database.ingestPodcast(e) map { _ =>
        AddPodcastResponse(Option(e))
      }
    }
  }
}
