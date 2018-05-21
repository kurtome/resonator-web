package kurtome.dote.server.inject

import com.google.inject.AbstractModule
import kurtome.dote.server.tasks.IndexPodcastsActor
import kurtome.dote.server.tasks.ProgramRadioStationsActor
import kurtome.dote.server.tasks.WarmDbActor
import kurtome.dote.server.tasks.{IngestPodcastsActor, SetPopularPodcastsActor}
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
