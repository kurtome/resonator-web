package kurtome.dote.server.tasks

import java.time.LocalDateTime

import javax.inject._
import akka.actor.{Actor, ActorRef, ActorSystem}
import kurtome.dote.server.search.SearchClient
import kurtome.dote.server.services.DotableService
import wvlet.log.LogSupport

import scala.concurrent.duration._
import scala.concurrent._

class IndexPodcastsTask @Inject()(
    actorSystem: ActorSystem,
    taskConfig: TaskConfig,
    @Named("index-podcasts") actor: ActorRef)(implicit executionContext: ExecutionContext)
    extends LogSupport {

  val run = taskConfig.isEnabled(this)

  if (run) {
    info("Enabling task.")
    actorSystem.scheduler.schedule(
      initialDelay = 10.seconds,
      interval = 1.hour,
      receiver = actor,
      message = IndexPodcasts
    )
  } else {
    info("Not enabled.")
  }
}

case object IndexPodcasts

@Singleton
class IndexPodcastsActor @Inject()(
    actorSystem: ActorSystem,
    dotableService: DotableService,
    searchClient: SearchClient)(implicit executionContext: ExecutionContext)
    extends Actor
    with LogSupport {

  override def receive = {
    case IndexPodcasts =>
      debug("Starting...")

      // index 1/12 of the podcasts, this runs once an hour so it should index each podcast twice a
      // day
      val modValue = LocalDateTime.now().getHour % 12

      dotableService.readAllPodcastIds().map(_.filter(_ % modValue == 0)) flatMap {
        podcastDotableIds =>
          info(s"Indexing ${podcastDotableIds.size} podcasts")
          Future.sequence(podcastDotableIds map { podcastId =>
            dotableService.readDotableDetails(podcastId, None) flatMap { podcast =>
              if (podcast.isDefined) {
                debug(s"Indexing ${podcast.get.getCommon.title}")
                searchClient.indexPodcastWithEpisodes(podcast.get)
              } else {
                Future(Unit)
              }
            }
          })
      } map { _ =>
        info("Finished.")
      }
  }
}
