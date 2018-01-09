package kurtome.dote.web.utils

import dote.proto.api.action.get_logged_in_person.GetLoggedInPersonRequest
import dote.proto.api.person.Person
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.shared.util.observer.{Observable, SimpleObservable}
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object LoggedInPersonManager extends LogSupport {

  case class LoginState(person: Option[Person])

  val stateObservable: Observable[LoginState] = SimpleObservable()

  private var state = LoginState(None)

  def curState = state

  def stateChanged(person: Option[Person]): Unit = {
    state = LoginState(person)
    stateObservable.notifyObservers(state)
  }

  private def getInitialData(): Unit = {
    val f = DoteProtoServer.getLoggedInPerson(GetLoggedInPersonRequest(true))
    GlobalLoadingManager.addLoadingFuture(f)

    f map { response =>
      stateChanged(response.person)
    }
  }

  getInitialData()
}
