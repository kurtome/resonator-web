package resonator.server.tasks

import java.net.UnknownHostException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

import akka.actor.{Actor, ActorRef, ActorSystem}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import javax.inject._
import akka.dispatch.RequiresMessageQueue
import akka.dispatch.BoundedMessageQueueSemantics
import resonator.server.ingestion.PodcastFeedIngester
import resonator.server.services.DotableService
import resonator.shared.util.result.FailedData
import resonator.shared.util.result.ProduceAction
import resonator.shared.util.result.UnknownErrorStatus
import resonator.slick.db.gen.Tables
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

// Message sent to Actor to request ingestion run
case object IngestPodcasts

@Singleton
class IngestPodcastsActor @Inject()(
    actorSystem: ActorSystem,
    taskConfig: TaskConfig,
    dotableDbService: DotableService,
    podcastFeedIngester: PodcastFeedIngester)(implicit executionContext: ExecutionContext)
    extends Actor
    with RequiresMessageQueue[BoundedMessageQueueSemantics]
    with LogSupport {

  private val batchSize = taskConfig.ingestionBatchSize
  private val inProgressIds = createSynchronizedSet[Long]()

  override def receive = {
    case IngestPodcasts =>
      debug("Starting podcast ingestion...")

      val ingestFutures = dotableDbService.getNextPodcastIngestionRows(batchSize) flatMap {
        ingestionRows =>
          debug(s"Found ${ingestionRows.size} to ingest.")

          if (inProgressIds.size <= batchSize / 10) {
            val newRows = ingestionRows.filterNot(row => inProgressIds.contains(row.id))

            info(s"""
            Found ${ingestionRows.size} rows.
            ${inProgressIds.size} total in progress already.
            Filtered ${ingestionRows.size - newRows.size} in progress.
            Starting ingestion for ${newRows.size}.
            """)

            Future.sequence(newRows.map(ingestPodcast))
          } else {
            info(s"Skipping new ingestion run since ${inProgressIds.size} are in progress.")
            Future(Unit)
          }
      }
      // Wait for this batch of ingestion to finish before allowing another to be scheduled
      Await.ready(ingestFutures, 2.minute)
  }

  private def ingestPodcast(row: Tables.PodcastFeedIngestionRow): Future[_] = {
    inProgressIds.add(row.id)
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
      // remove from in progress
      inProgressIds.remove(row.id)

      if (response.isError) {
        // Something went wrong or there was no valid podcast in the feed, set the next ingestion
        // time so this doesn't get reprocessed over and over again.
        debug(s"Setting next ingestion time for error row $row")
        dotableDbService.updateNextIngestionTimeByItunesId(
          row.itunesId,
          LocalDateTime.now().plusMinutes(row.reingestWaitMinutes))
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

  def createSynchronizedSet[T](): scala.collection.mutable.Set[T] = {
    import scala.collection.JavaConverters._
    java.util.Collections
      .newSetFromMap(new java.util.concurrent.ConcurrentHashMap[T, java.lang.Boolean])
      .asScala
  }
}
