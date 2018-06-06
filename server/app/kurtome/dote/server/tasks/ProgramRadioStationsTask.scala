package kurtome.dote.server.tasks

import java.time.LocalDateTime

import javax.inject._
import akka.actor.{Actor, ActorRef, ActorSystem}
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.server.db.RadioDbIo
import kurtome.dote.server.search.SearchClient
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.services.RadioService
import kurtome.dote.server.services.SearchIndexQueueService
import kurtome.dote.server.util.UrlIds
import kurtome.dote.slick.db.gen.Tables
import wvlet.log.LogSupport

import scala.concurrent.duration._
import scala.concurrent._
import scala.util.Random

class ProgramRadioStationsTask @Inject()(
    actorSystem: ActorSystem,
    taskConfig: TaskConfig,
    @Named("program-radio-stations") actor: ActorRef)(implicit executionContext: ExecutionContext)
    extends LogSupport {

  val run = taskConfig.isEnabled(this)

  if (run) {
    info("Enabling task.")
    actorSystem.scheduler.schedule(
      initialDelay = 5.seconds,
      interval = 5.minute,
      receiver = actor,
      message = ProgramRadioStations
    )
  } else {
    info("Not enabled.")
  }
}

case object ProgramRadioStations

@Singleton
class ProgramRadioStationsActor @Inject()(
    actorSystem: ActorSystem,
    radioService: RadioService,
    dotableService: DotableService)(implicit executionContext: ExecutionContext)
    extends Actor
    with LogSupport {

  override def receive = {
    case ProgramRadioStations =>
      debug("Starting...")

      (for {
        stations <- radioService.readAllStations()
        _ <- seqFutures(stations)(programStation)
        _ = {
          debug(s"${stations.size} stations")
        }
      } yield {
        info("Finished.")
        Unit
      }) recover {
        case t => warn("Error while programming stations", t)
      }
  }

  private def programStation(station: Tables.RadioStationRow): Future[Unit] = {
    for {
      latestEpisode <- radioService.readLatestPlaylistEntry(station.id)
      podcastIds <- radioService.readPodcastsForStation(station.id)
      _ <- {
        debug(s"${station.id} $latestEpisode")
        val shouldAddEpisode =
          latestEpisode.forall(_.startTime.isBefore(LocalDateTime.now().plusHours(2)))

        if (shouldAddEpisode) {
          for {
            podcastOpt <- dotableService.readPodcastWithEpisodes(randomElement(podcastIds))
            podcast = podcastOpt.get
            episode = chooseEpisode(station, podcast)
            _ <- {
              debug(s"adding episode ${episode.getCommon.title} from ${podcast.getCommon.title}")
              val nextStartTime = latestEpisode
                .map(_.endTime)
                // ignore old end times, and allow for a gap (could happen if this job doesn't run
                // for a while)
                .filterNot(_.isBefore(LocalDateTime.now()))
                .getOrElse(LocalDateTime.now())
              val nextEndTime =
                nextStartTime.plusSeconds(episode.getDetails.getPodcastEpisode.durationSec)
              val newRow = Tables.RadioStationPlaylistRow(
                id = 0,
                stationId = station.id,
                episodeId = UrlIds.decodeDotable(episode.id),
                startTime = nextStartTime,
                endTime = nextEndTime,
                dbUpdatedTime = LocalDateTime.MIN
              )
              debug(s"adding row $newRow")
              radioService.createStationPlaylistEntry(newRow)
            }
          } yield ()
        } else {
          debug("not adding episode")
          Future.unit
        }
      }
    } yield ()
  }

  def chooseEpisode(station: Tables.RadioStationRow, podcast: Dotable): Dotable = {
    val episodes = podcast.getRelatives.children.filter(isValidEpisode)
    if (station.callSign == "QTLK" || station.callSign == "QSTP") {
      // News station or sports, always pick latest episode
      episodes.head
    } else {
      randomElement(episodes)
    }
  }

  def randomElement[T](xs: Seq[T]): T = {
    xs(Random.nextInt(xs.length))
  }

  def isValidEpisode(dotable: Dotable): Boolean = {
    val episodeDetails = dotable.getDetails.getPodcastEpisode
    episodeDetails.durationSec > (10 * 60) // should be at least 10 minutes long
  }

  def seqFutures[T, U](items: TraversableOnce[T])(fn: T => Future[U]): Future[List[U]] = {
    items.foldLeft(Future.successful[List[U]](Nil)) { (f, item) =>
      f.flatMap { x =>
        fn(item).map(_ :: x)
      }
    } map (_.reverse)
  }
}
