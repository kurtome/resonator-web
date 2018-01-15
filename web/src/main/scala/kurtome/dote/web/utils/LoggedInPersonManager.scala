package kurtome.dote.web.utils

import dote.proto.api.action.get_logged_in_person.GetLoggedInPersonRequest
import dote.proto.api.person.Person
import kurtome.dote.shared.util.observer.{Observable, SimpleObservable}
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.shared.util.observer.SimpleObservable
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object LoggedInPersonManager extends LogSupport {

  case class LoginState(person: Option[Person], fetched: Boolean = false)

  val stateObservable: Observable[LoginState] = SimpleObservable()

  private val cookieValues = dom.document.cookie.split("; ")
  // check for the cookie written by the redirect controller
  val loginAttempted = cookieValues.contains("LOGIN_REDIRECT=")
  private var state = LoginState(None, false)

  def curState = state

  def stateChanged(person: Option[Person]): Unit = {
    state = LoginState(person = person, fetched = true)
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
