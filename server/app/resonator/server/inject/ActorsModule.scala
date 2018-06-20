package resonator.server.inject

import com.google.inject.AbstractModule
import resonator.server.tasks.IndexPodcastsActor
import resonator.server.tasks.ProgramRadioStationsActor
import resonator.server.tasks.WarmDbActor
import resonator.server.tasks.{IngestPodcastsActor, SetPopularPodcastsActor}
import play.api.libs.concurrent.AkkaGuiceSupport

class ActorsModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[ProgramRadioStationsActor]("program-radio-stations")
    bindActor[IngestPodcastsActor]("ingest-podcasts")
    bindActor[IndexPodcastsActor]("index-podcasts")
    bindActor[SetPopularPodcastsActor]("set-popular-podcasts")
    bindActor[WarmDbActor]("warm-db")
  }
}
