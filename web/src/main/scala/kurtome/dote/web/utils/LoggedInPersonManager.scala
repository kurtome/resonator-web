package kurtome.dote.web.utils

import kurtome.dote.proto.api.person.Person
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js.JSON

object LoggedInPersonManager extends LogSupport {

  private def loadInitialState(): Option[Person] = {
    val personElement = dom.document.body.querySelector("#logged-in-holder")
    val personString = personElement.innerHTML

    if (personString.nonEmpty) {
      // can't figure out how to get the ScalaPB JsonFormat class to compile for scala.js, so just
      // do the mapping by hand
      val personJson = JSON.parse(personString)
      val person = Person(
        id = personJson.id.asInstanceOf[String],
        username = personJson.username.asInstanceOf[String],
        email = personJson.email.asInstanceOf[String]
      )
      Some(person)
    } else {
      None
    }
  }

  val person = loadInitialState()

  val isLoggedIn: Boolean = person.isDefined

  private val cookieValues = dom.document.cookie.split("; ")
  // check for the cookie written by the redirect controller
  val loginAttempted = cookieValues.contains("LOGIN_REDIRECT=")

  var displayedLoginSnack: Boolean = false
}
