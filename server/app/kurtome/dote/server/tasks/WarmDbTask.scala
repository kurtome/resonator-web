package kurtome.dote.server.tasks

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import javax.inject._
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.HomeId
import kurtome.dote.server.controllers.feed.FeedParams
import kurtome.dote.server.controllers.feed.HomeFeedFetcher
import kurtome.dote.server.services.PersonService
import wvlet.log.LogSupport

import scala.concurrent._
import scala.concurrent.duration._

/**
  * This task just runs the query that powers the home page in order to ensure the database has the
  * correct indices / tables in memory to allow for quick retrieval of the home page data. This
  * is necessary because if no one visits the site for a while the only tables in memory are the
  * ingestion tables.
  */
class WarmDbTask @Inject()(
    actorSystem: ActorSystem,
    taskConfig: TaskConfig,
    @Named("warm-db") actor: ActorRef)(implicit executionContext: ExecutionContext)
    extends LogSupport {

  val run = taskConfig.isEnabled(this)

  if (run) {
    info("Enabling task.")
    actorSystem.scheduler.schedule(
      initialDelay = 1.second,
      interval = 10.minutes,
      receiver = actor,
      message = WarmDb
    )
  } else {
    info("Not enabled.")
  }
}

case object WarmDb

@Singleton
class WarmDbActor @Inject()(
    actorSystem: ActorSystem,
    homeFeedFetcher: HomeFeedFetcher,
    personService: PersonService)(implicit executionContext: ExecutionContext)
    extends Actor
    with LogSupport {

  override def receive = {
    case WarmDb =>
      debug("Starting...")

      for {
        person <- personService.readById(1)
        params = FeedParams(loggedInUser = person,
                            maxItemSize = 10,
                            feedId = FeedId().withHome(HomeId()))
        feed <- homeFeedFetcher.fetch(params)
      } yield {
        info(s"fetched home feed with ${feed.items.length} items")
      }
  }
}
