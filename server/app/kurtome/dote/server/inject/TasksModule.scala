package kurtome.dote.server.inject

import kurtome.dote.server.tasks._
import play.api.inject._

class TasksModule
    extends SimpleModule(
      bind[TaskConfig].toSelf,
      bind[ProgramRadioStationsTask].toSelf.eagerly,
      bind[IngestPodcastsTask].toSelf.eagerly,
      bind[IndexPodcastsTask].toSelf.eagerly,
      bind[SetPopularPodcastsTask].toSelf.eagerly,
      bind[WarmDbTask].toSelf.eagerly
    )
