package resonator.server.tasks

import javax.inject._
import akka.actor.{Actor, ActorRef, ActorSystem}
import resonator.server.ingestion.PodcastFeedIngester
import resonator.server.model.MetadataFlag
import resonator.server.search.SearchClient
import resonator.server.services.{DotableService, DoteService}
import resonator.shared.constants.DotableKinds
import wvlet.log.LogSupport

import scala.concurrent.duration._
import scala.concurrent._

class SetPopularPodcastsTask @Inject()(
    actorSystem: ActorSystem,
    taskConfig: TaskConfig,
    @Named("set-popular-podcasts") actor: ActorRef)(implicit executionContext: ExecutionContext)
    extends LogSupport {

  val run = taskConfig.isEnabled(this)

  if (run) {
    info("Enabling task.")
    actorSystem.scheduler.schedule(
      initialDelay = 10.seconds,
      interval = 1.hour,
      receiver = actor,
      message = SetPopularPodcasts
    )
  } else {
    info("Not enabled.")
  }
}

case object SetPopularPodcasts

@Singleton
class SetPopularPodcastsActor @Inject()(
    actorSystem: ActorSystem,
    dotableService: DotableService,
    doteService: DoteService,
    podcastFeedIngester: PodcastFeedIngester)(implicit executionContext: ExecutionContext)
    extends Actor
    with LogSupport {

  private val popularLimit = 100

  override def receive = {
    case SetPopularPodcasts =>
      debug("Starting...")
      doteService.readPopular(DotableKinds.Podcast, popularLimit) map { dotables =>
        dotableService.replaceMetadataTagList(MetadataFlag.Keys.popular, dotables.map(_.id)) map {
          _ =>
            info(s"successfully set ${dotables.size} podcasts as popular")
        }
      }
  }
}
