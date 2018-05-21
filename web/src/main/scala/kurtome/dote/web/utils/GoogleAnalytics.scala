package kurtome.dote.web.utils

import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * https://www.npmjs.com/package/universal-analytics
  */
object UniversalAnalytics extends LogSupport {

  @JSImport("universal-analytics", JSImport.Default)
  @js.native
  private object ua extends js.Object {
    def apply(id: String, options: js.Dynamic): Visitor = js.native

    def apply(id: String, userId: String, options: js.Dynamic): Visitor = js.native
  }

  @js.native
  trait Visitor extends js.Object {
    def pageview(path: String): VisitorPageView = js.native
  }

  @js.native
  trait VisitorPageView extends js.Object {
    def send(): Unit = js.native
  }

  def apply(id: String, options: js.Dynamic): Visitor = {
    ua(id, options)
  }

  def apply(id: String, userId: String, options: js.Dynamic): Visitor = {
    ua(id, userId, options)
  }

  val visitor = {
    val options = js.Dynamic.literal(
      "https" -> (dom.window.location.protocol == "https:")
    )
    if (LoggedInPersonManager.isLoggedIn) {
      UniversalAnalytics("UA-118773848-1",
                         s"ruser:${LoggedInPersonManager.person.get.id}",
                         options)
    } else {
      UniversalAnalytics("UA-118773848-1", options)
    }
  }
}
