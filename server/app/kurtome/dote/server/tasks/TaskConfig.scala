package kurtome.dote.server.tasks

import javax.inject._

import play.api.Configuration

@Singleton
class TaskConfig @Inject()(config: Configuration) {

  val tasksEnabled: Boolean = config.get[Boolean]("kurtome.dote.tasks.enabled")

  def isEnabled(task: AnyRef): Boolean = {
    val taskName = task.getClass.getSimpleName
    val taskKey = s"kurtome.dote.tasks.$taskName.enabled"
    tasksEnabled && config.getOptional[Boolean](taskKey).getOrElse(false)
  }

  lazy val ingestionBatchSize: Int =
    config.get[String]("kurtome.dote.tasks.IngestPodcastsTask.batchSize").toInt

}
