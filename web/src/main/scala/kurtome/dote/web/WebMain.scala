package kurtome.dote.web

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import CssSettings._
import wvlet.log._

import scala.scalajs.LinkingInfo

object WebMain extends LogSupport {

  val Hello =
    ScalaComponent
      .builder[String]("Hello")
      .render_P(name => <.div("Hello there ", name))
      .build

  private var rootNode: dom.Element = null

  def getRootNode = rootNode

  def main(args: Array[String]): Unit = {
    // Attach both style files to the head
    SharedStyles.addToDocument()
    attachStandaloneStyle(StandaloneStyles)

    Logger.setDefaultHandler(new JSConsoleLogHandler())
    if (LinkingInfo.productionMode) {
      Logger.setDefaultLogLevel(LogLevel.WARN)
    } else {
      Logger.setDefaultLogLevel(LogLevel.DEBUG)
      debug("dev logging enabled.")
    }

    rootNode = dom.document.body.querySelector("#reactroot")

    DoteRoutes.router().renderIntoDOM(rootNode)
  }

  /**
    * Inject stylesheet into the DOM.
    */
  private def attachStandaloneStyle(stylesheet: StyleSheet.Standalone): Unit = {
    val rawCssStr: String = stylesheet.render
    val styleElement = dom.document.createElement("style")
    styleElement.innerHTML = rawCssStr
    dom.document.head.appendChild(styleElement)
  }

}
