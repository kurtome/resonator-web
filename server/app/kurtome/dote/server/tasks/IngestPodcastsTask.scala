package kurtome.dote.server.tasks

import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef, ActorSystem}
import kurtome.dote.server.tasks.IngestPodcastsActor.IngestPodcasts

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import javax.inject._

import dote.proto.api.action.add_podcast.AddPodcastResponse
import kurtome.dote.server.db.DotableDbService
import kurtome.dote.server.ingestion.PodcastFeedIngester
import play.api.Configuration
import play.libs.Akka
import wvlet.log.LogSupport

import scala.util.Try

class IngestPodcastsTask @Inject()(
    actorSystem: ActorSystem,
    config: Configuration,
    @Named("ingest-podcasts") actor: ActorRef)(implicit executionContext: ExecutionContext)
    extends LogSupport {

  val runBackgroundIngestion = config.get[Boolean]("kurtome.dote.ingestion.background.run")

  if (runBackgroundIngestion) {
    info("Running background ingestion.")
    actorSystem.scheduler.schedule(
      initialDelay = 10.seconds,
      interval = 1.minutes,
      receiver = actor,
      message = IngestPodcasts
    )
  } else {
    info("Not running background ingestion.")
  }
}

object IngestPodcastsActor {
  case object IngestPodcasts
}
@Singleton
class IngestPodcastsActor @Inject()(actorSystem: ActorSystem,
                                    dotableDbService: DotableDbService,
                                    podcastFeedIngester: PodcastFeedIngester)
    extends Actor
    with LogSupport {

  implicit val myExecutionContext: ExecutionContext =
    actorSystem.dispatchers.lookup("ingestion-context")

  override def receive = {
    case IngestPodcasts =>
      debug("Starting podcast ingestion...")

      dotableDbService.getNextPodcastIngestionRows(100) map { ingestionRows =>
        debug(s"Found ${ingestionRows.size} to ingest.")

        ingestionRows foreach { row =>
          val response: AddPodcastResponse = Try {
            debug(s"Ingesting $row")
            // use Await to only ingest one at a time to not hog all the DB connections and threads.
            Await.result(podcastFeedIngester.reingestPodcastByItunesId(row.itunesId) map {
              result =>
                debug(s"Finished ingesting $row")
                result
            }, atMost = 10.seconds)
          } recover {
            case t: Throwable =>
              error(s"Exception ingesting $row", t)
              AddPodcastResponse.defaultInstance
          } get

          if (response.podcasts.isEmpty) {
            // Something went wrong or there was no valid podcast in the feed, set the next ingestion
            // time so this doesn't get reprocessed over and over again.
            info(s"Setting next ingestion time for error row $row")
            dotableDbService.updateNextIngestionTimeByItunesId(row.itunesId,
                                                               LocalDateTime.now().plusHours(6))
          }
        }
      }
  }
}
