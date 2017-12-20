package kurtome.dote.web.utils

import kurtome.dote.web.shared.util.observer._

object GlobalNotificationManager {

  case class Notification(message: String)

  val stateObservable: Observable[Notification] = SimpleObservable()

  def displayMessage(message: String): Unit = {
    stateObservable.notifyObservers(Notification(message))
  }

}
