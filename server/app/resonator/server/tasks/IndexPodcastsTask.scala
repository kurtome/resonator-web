package resonator.server.tasks

import javax.inject._
import akka.actor.{Actor, ActorRef, ActorSystem}
import resonator.server.search.SearchClient
import resonator.server.services.DotableService
import resonator.server.services.SearchIndexQueueService
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
      initialDelay = 5.seconds,
      interval = 2.minute,
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
    searchIndexQueueService: SearchIndexQueueService,
    searchClient: SearchClient)(implicit executionContext: ExecutionContext)
    extends Actor
    with LogSupport {

  override def receive = {
    case IndexPodcasts =>
      debug("Starting...")

      val approxBatchSize = 500

      (for {
        queueRow <- searchIndexQueueService.readDotableRow()
        timestamp = queueRow.syncCompletedThroughTime
        id = queueRow.lastBatchMaxId
        (dotables, newMaxTimestamp, newMaxId) <- dotableService
          .readBatchByNextMaxUpdatedTime(approxBatchSize, timestamp, id)
        _ <- {
          info(s"Indexing ${dotables.size} dotables")
          searchClient.indexDotables(dotables)
        }
        _ <- {
          debug(s"Old timestamp: $timestamp, new timestamp: $newMaxTimestamp")
          debug(s"Old ID: $id, new ID: $newMaxId")
          searchIndexQueueService.writeDotableRow(newMaxTimestamp, newMaxId)
        }
      } yield {
        info("Finished.")
        Unit
      }) recover {
        case t => warn("Error while indexing", t)
      }
  }

  def seqFutures[T, U](items: TraversableOnce[T])(fn: T => Future[U]): Future[List[U]] = {
    items.foldLeft(Future.successful[List[U]](Nil)) { (f, item) =>
      f.flatMap { x =>
        fn(item).map(_ :: x)
      }
    } map (_.reverse)
  }
}
