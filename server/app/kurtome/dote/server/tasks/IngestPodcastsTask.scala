package kurtome.dote.server.tasks

import java.net.UnknownHostException
import java.time.LocalDateTime

import akka.actor.{Actor, ActorRef, ActorSystem}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import javax.inject._

import akka.dispatch.RequiresMessageQueue
import akka.dispatch.BoundedMessageQueueSemantics
import kurtome.dote.server.ingestion.PodcastFeedIngester
import kurtome.dote.server.services.DotableService
import kurtome.dote.shared.util.result.FailedData
import kurtome.dote.shared.util.result.ProduceAction
import kurtome.dote.shared.util.result.UnknownErrorStatus
import kurtome.dote.slick.db.gen.Tables
import wvlet.log._

import scala.annotation.tailrec
import scala.concurrent.Future

class IngestPodcastsTask @Inject()(
    actorSystem: ActorSystem,
    taskConfig: TaskConfig,
    @Named("ingest-podcasts") actor: ActorRef)(implicit executionContext: ExecutionContext)
    extends LogSupport {

  val run = taskConfig.isEnabled(this)

  if (run) {
    info("Enabling task.")
    actorSystem.scheduler.schedule(
      initialDelay = 10.seconds,
      interval = 1.minutes,
      receiver = actor,
      message = IngestPodcasts
    )
  } else {
    info("Not enabled.")
  }
}

case object IngestPodcasts
@Singleton
class IngestPodcastsActor @Inject()(
    actorSystem: ActorSystem,
    dotableDbService: DotableService,
    podcastFeedIngester: PodcastFeedIngester)(implicit executionContext: ExecutionContext)
    extends Actor
    with RequiresMessageQueue[BoundedMessageQueueSemantics]
    with LogSupport {

  override def receive = {
    case IngestPodcasts =>
      debug("Starting podcast ingestion...")

      val ingestFutures = dotableDbService.getNextPodcastIngestionRows(1000) flatMap {
        ingestionRows =>
          debug(s"Found ${ingestionRows.size} to ingest.")
          Future.sequence(ingestionRows.map(ingestPodcast))
      }
      // Wait for this batch of ingestion to finish before allowing another to be scheduled
      Await.ready(ingestFutures, 2.minute)
  }

  private def ingestPodcast(row: Tables.PodcastFeedIngestionRow): Future[_] = {
    debug(s"Ingesting $row")
    val responseFuture: Future[ProduceAction[Seq[Long]]] =
      // use Await to only ingest one at a time to not hog all the DB connections and threads.
      podcastFeedIngester.reingestPodcastByItunesId(row.itunesId) map { result =>
        debug(s"Finished ingesting $row")
        result
      } recover {
        case t: Throwable =>
          val cause = getCause(t)
          warn(
            s"Exception ingesting $row. ${t.getClass.getSimpleName} caused by ${cause.getClass.getName}: ${cause.getMessage}")
          FailedData(Nil, UnknownErrorStatus)
      }

    responseFuture map { response =>
      if (response.isError) {
        // Something went wrong or there was no valid podcast in the feed, set the next ingestion
        // time so this doesn't get reprocessed over and over again.
        debug(s"Setting next ingestion time for error row $row")
        dotableDbService.updateNextIngestionTimeByItunesId(row.itunesId,
                                                           LocalDateTime.now().plusHours(6))
      }
    }
  }

  // TODO: move this to a util
  @tailrec
  private def getCause(e: Throwable): Throwable = {
    val cause = e.getCause
    if (cause == null || cause.eq(e)) {
      e
    } else {
      getCause(cause)
    }
  }
}
