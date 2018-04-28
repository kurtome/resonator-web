package kurtome.dote.server.tasks

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.chrono.ChronoLocalDateTime

import javax.inject._
import akka.actor.{Actor, ActorRef, ActorSystem}
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.server.search.SearchClient
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.services.SearchIndexQueueService
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
      interval = 1.minute,
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
        timestamp <- searchIndexQueueService.readDotableSyncCompletedTimestamp()
        dotablesIdsWithTimestamps <- dotableService.readNextOldestModifiedBatch(approxBatchSize,
                                                                                timestamp)
        newMaxTimestamp = dotablesIdsWithTimestamps
          .map(_._2)
          .sortWith((l1, l2) => (l1 compareTo l2) < 0)
          .last
        dotableIds = dotablesIdsWithTimestamps.map(_._1)
        dotables <- dotableService
          .readBatchById(dotableIds)
          .map(_.filter(_.kind match {
            case Dotable.Kind.PODCAST => true
            case Dotable.Kind.PODCAST_EPISODE => true
            // ignore everything else
            case _ => false
          }))
        _ <- {
          info(s"Indexing ${dotables.size} dotables")
          searchClient.indexDotables(dotables)
        }
        _ <- {
          info(s"Old timestamp: $timestamp, new timestamp: $newMaxTimestamp")
          searchIndexQueueService.writeDotableSyncCompletedTimestamp(newMaxTimestamp)
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
