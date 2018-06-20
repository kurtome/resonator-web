package resonator.web.utils

import resonator.shared.util.observer._

object GlobalNotificationManager {

  object NotificationKinds extends Enumeration {
    val Standard = Value
    val Error = Value
  }
  type NotificationKind = NotificationKinds.Value

  case class Notification(message: String, kind: NotificationKind)

  val stateObservable: Observable[Notification] = SimpleObservable()

  def display(message: String, kind: NotificationKind = NotificationKinds.Standard): Unit = {
    stateObservable.notifyObservers(Notification(message, kind))
  }

  def displayMessage(message: String): Unit = {
    display(message, NotificationKinds.Standard)
  }

  def displayError(message: String): Unit = {
    display(message, NotificationKinds.Error)
  }

}
