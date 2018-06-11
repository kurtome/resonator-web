package kurtome.dote.web.utils

import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import scala.scalajs.js.annotation.JSGlobalScope
import scala.scalajs.js.annotation.JSImport

/**
  * https://developers.google.com/analytics/devguides/collection/gtagjs/pages
  */
object UniversalAnalytics extends LogSupport {

  val id = "UA-118773848-1"

  @js.native
  @JSGlobal("gtag")
  private object gtag extends js.Object {
    def apply(command: String, id: String, params: js.Dynamic): Unit = js.native
  }

  def pageview(): Unit = {
    gtag("config",
         id,
         js.Dynamic.literal(
           "page_path" -> (dom.window.location.pathname + dom.window.location.search),
           "page_location" -> dom.window.location.href
         ))
  }

  def event(category: String, action: String, label: String): Unit = {
    gtag("event",
         action,
         js.Dynamic.literal(
           "event_category" -> category,
           "event_label" -> label
         ))
  }

}
