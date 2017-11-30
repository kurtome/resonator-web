package kurtome.dote.server.inject

import kurtome.dote.server.tasks.IngestPodcastsTask
import play.api.inject._

class TasksModule extends SimpleModule(bind[IngestPodcastsTask].toSelf.eagerly)
