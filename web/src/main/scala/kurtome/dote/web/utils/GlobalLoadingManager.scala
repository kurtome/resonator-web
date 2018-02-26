package kurtome.dote.web.utils

import kurtome.dote.shared.util.observer._
import kurtome.dote.web.utils.GlobalNotificationManager.NotificationKinds
import wvlet.log.LogSupport

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object GlobalLoadingManager extends LogSupport {

  case class State(inFlight: Seq[Future[_]] = Nil) {
    def isLoading = inFlight.nonEmpty
  }

  val stateObservable: Observable[State] = SimpleObservable()
  private var state = State()

  def curState = state

  /**
    * Sets the current state to be loading until the [[Future]] input is finished.
    */
  def addLoadingFuture(f: Future[_],
                       displayError: Boolean = true,
                       errorMessage: String = "Network request failed."): Unit = {
    state = state.copy(inFlight = state.inFlight :+ f)

    f andThen {
      case _ =>
        state = state.copy(inFlight = state.inFlight.filter(_ != f))
        stateObservable.notifyObservers(state)
    }

    f recover {
      case t =>
        debug(s"async future, something went wrong. ${t.getClass.getSimpleName}: ${t.getMessage}")
        if (displayError) {
          GlobalNotificationManager.displayError(errorMessage)
        }
    }

    stateObservable.notifyObservers(state)
  }
}
