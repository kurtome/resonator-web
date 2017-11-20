package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.server.controllers.podcast.{
  ItunesEntityFetcher,
  PodcastFeedFetcher,
  RssFetchedPodcast
}
import dote.proto.api.action.add_podcast._
import dote.proto.api.dotable.Dotable
import kurtome.dote.server.db.PodcastDbService
import play.api.{Configuration, Logger}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddPodcastController @Inject()(
    cc: ControllerComponents,
    config: Configuration,
    itunesEntityFetcher: ItunesEntityFetcher,
    podcastFetcher: PodcastFeedFetcher,
    podcastDbService: PodcastDbService)(implicit ec: ExecutionContext)
    extends ProtobufController[AddPodcastRequest, AddPodcastResponse](cc) {

  val allowExtras = config.get[Boolean]("kurtome.dote.add.podcast.extras")

  override def parseRequest(bytes: Array[Byte]): AddPodcastRequest = {
    val req = AddPodcastRequest.parseFrom(bytes)
    if (allowExtras) {
      req
    } else {
      req.update(_.extras := AddPodcastRequest.Extras.defaultInstance)
    }
  }

  override def action(request: AddPodcastRequest): Future[AddPodcastResponse] = {
    val itunesIdStr = request.itunesUrl.split('/').last.substring(2).replaceAll("\\?.*", "")
    val itunesId = itunesIdStr.toLong
    itunesEntityFetcher.fetch(itunesId, "podcast") flatMap { itunesEntity =>
      assert(itunesEntity.resultCount == 1, "must have exactly 1 result")
      val entity = itunesEntity.results.head
      fetchFeedAndIngest(request.getExtras, itunesId, entity.trackViewUrl, entity.feedUrl)
    }
  }

  private def fetchFeedAndIngest(extras: AddPodcastRequest.Extras,
                                 itunesId: Long,
                                 itunesUrl: String,
                                 feedUrl: String): Future[AddPodcastResponse] = {
    val eventualPodcast: Future[Seq[RssFetchedPodcast]] = podcastFetcher.fetch(itunesUrl, feedUrl)

    eventualPodcast flatMap { rssPodcasts =>
      Future.sequence(rssPodcasts.map(ingestToDatabase(extras, itunesId, _)))
    } flatMap { podcastIds =>
      Future.sequence(podcastIds.map(podcastDbService.readPodcastWithEpisodes(_)))
    } map { podcasts =>
      AddPodcastResponse(podcasts.filter(_.isDefined).map(_.get))
    }
  }

  private def ingestToDatabase(extras: AddPodcastRequest.Extras,
                               itunesId: Long,
                               podcast: RssFetchedPodcast): Future[Long] = {
    val id = podcastDbService.ingestPodcast(itunesId, podcast)
    if (extras.popular) {
      id.map(podcastDbService.setPopularTag(_))
    }
    id
  }

}
