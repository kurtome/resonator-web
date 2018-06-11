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

  val username = person.map(_.username).getOrElse("")

  val isLoggedIn: Boolean = person.isDefined

  val isNotLoggedIn: Boolean = person.isEmpty

  // anything important is double checked on the server, so it's ok to use this on the client
  // to hide things from non-admins
  val isAdmin: Boolean = person.map(_.id).getOrElse("") == "aDBijX"

  def isLoggedInPerson(username: String): Boolean = {
    isLoggedIn && username == person.get.username
  }

  private val cookieValues = dom.document.cookie.split("; ")
  // check for the cookie written by the redirect controller
  val loginAttempted: Boolean = cookieValues.contains("LOGIN_REDIRECT=")

  var displayedLoginSnack: Boolean = false
}
