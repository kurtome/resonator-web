package kurtome.dote.server.inject

import kurtome.dote.server.tasks._
import play.api.inject._

class TasksModule
    extends SimpleModule(bind[TaskConfig].toSelf,
                         bind[IngestPodcastsTask].toSelf.eagerly,
                         bind[SetPopularPodcastsTask].toSelf.eagerly)
