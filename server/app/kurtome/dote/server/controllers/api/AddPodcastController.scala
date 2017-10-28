package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.server.controllers.podcast.{PodcastFeedFetcher, RssFetchedPodcast}
import dote.proto.api.action.add_podcast._
import dote.proto.api.dotable._
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

  override def action(request: AddPodcastRequest): Future[AddPodcastResponse] = {
    val eventualPodcast: Future[Seq[RssFetchedPodcast]] = podcastFetcher.fetch(request.feedUrl)

    eventualPodcast flatMap { rssPodcasts =>
      Future.sequence(rssPodcasts.map(database.ingestPodcast(_)))
    } map { podcasts =>
      AddPodcastResponse(podcasts)
    }
  }
}
