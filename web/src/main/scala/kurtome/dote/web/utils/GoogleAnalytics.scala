package kurtome.dote.web.utils

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * https://www.npmjs.com/package/universal-analytics
  */
object UniversalAnalytics {

  @JSImport("universal-analytics", JSImport.Default)
  @js.native
  private object ua extends js.Object {
    def apply(id: String): Visitor = js.native
  }

  @js.native
  trait Visitor extends js.Object {
    def pageview(path: String): VisitorPageView = js.native
  }

  @js.native
  trait VisitorPageView extends js.Object {
    def send(): Unit = js.native
  }

  def apply(id: String): Visitor = {
    ua(id)
  }

  val visitor = UniversalAnalytics("UA-118773848-1")
}
