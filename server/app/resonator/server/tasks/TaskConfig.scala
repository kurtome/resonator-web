package resonator.server.tasks

import javax.inject._

import play.api.Configuration

@Singleton
class TaskConfig @Inject()(config: Configuration) {

  val tasksEnabled: Boolean = config.get[Boolean]("resonator.tasks.enabled")

  def isEnabled(task: AnyRef): Boolean = {
    val taskName = task.getClass.getSimpleName
    val taskKey = s"resonator.tasks.$taskName.enabled"
    tasksEnabled && config.getOptional[Boolean](taskKey).getOrElse(false)
  }

  lazy val ingestionBatchSize: Int =
    config.get[String]("resonator.tasks.IngestPodcastsTask.batchSize").toInt

}
