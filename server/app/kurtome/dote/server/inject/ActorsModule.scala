package kurtome.dote.server.inject

import com.google.inject.AbstractModule
import kurtome.dote.server.tasks.IngestPodcastsActor
import play.api.libs.concurrent.AkkaGuiceSupport

class ActorsModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[IngestPodcastsActor]("ingest-podcasts")
  }
}
