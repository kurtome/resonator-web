package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.server.controllers.podcast.{
  ItunesEntityFetcher,
  PodcastFeedFetcher,
  RssFetchedPodcast
}
import dote.proto.api.action.add_podcast._
import kurtome.dote.server.db.PodcastDbService
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddPodcastController @Inject()(
    cc: ControllerComponents,
    itunesEntityFetcher: ItunesEntityFetcher,
    podcastFetcher: PodcastFeedFetcher,
    podcastDbService: PodcastDbService)(implicit ec: ExecutionContext)
    extends ProtobufController[AddPodcastRequest, AddPodcastResponse](cc) {

  // Full URLs look like this
  // "http://itunes.apple.com/us/podcast/reply-all/id941907967?mt=2"
  val itunePodcastUrlPrefix = "itunes.apple.com/us/podcast/reply-all/id941907967?mt=2"

  override def parseRequest(bytes: Array[Byte]) =
    AddPodcastRequest.parseFrom(bytes)

  override def action(request: AddPodcastRequest): Future[AddPodcastResponse] = {

    val itunesIdStr = request.itunesUrl.split('/').last.substring(2).replaceAll("\\?.*", "")
    Logger.info(s"id:$itunesIdStr")

    itunesEntityFetcher.fetch(itunesIdStr.toLong, "podcast") flatMap { itunesEntity =>
      assert(itunesEntity.resultCount == 1, "must have exactly 1 result")
      fetchFeedAndIngest(itunesEntity.results.head.feedUrl)
    }
  }

  private def fetchFeedAndIngest(feedUrl: String): Future[AddPodcastResponse] = {
    val eventualPodcast: Future[Seq[RssFetchedPodcast]] = podcastFetcher.fetch(feedUrl)

    eventualPodcast flatMap { rssPodcasts =>
      Future.sequence(rssPodcasts.map(podcastDbService.ingestPodcast(_)))
    } flatMap { podcastIds =>
      Future.sequence(podcastIds.map(podcastDbService.readPodcastWithEpisodes(_)))
    } map { podcasts =>
      AddPodcastResponse(podcasts.filter(_.isDefined).map(_.get))
    }
  }

}
