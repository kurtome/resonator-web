package resonator.web

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import CssSettings._
import resonator.web.constants.MuiTheme
import org.scalajs.dom.html.Meta
import org.scalajs.dom.html.Style
import wvlet.log._

import scala.scalajs.LinkingInfo
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("WebMain")
object WebMain extends LogSupport {

  private var rootNode: dom.Element = null

  def getRootNode = rootNode

  @JSExport
  def main(): Unit = {
    Logger.setDefaultHandler(new JSConsoleLogHandler())
    if (LinkingInfo.productionMode) {
      Logger.setDefaultLogLevel(LogLevel.WARN)
    } else {
      Logger.setDefaultLogLevel(LogLevel.DEBUG)
      debug("dev logging enabled.")
    }

    attachThemeColor()
    refreshStyles()

    rootNode = dom.document.body.querySelector("#reactroot")

    DoteRoutes.doteRouter().renderIntoDOM(rootNode)
  }

  def refreshStyles() = {
    // Attach both style files to the head
    SharedStyles.addToDocument()
    attachStandaloneStyle(new StandaloneStyles)
  }

  /**
    * Inject stylesheet into the DOM.
    */
  private def attachStandaloneStyle(stylesheet: StyleSheet.Standalone): Unit = {
    val rawCssStr: String = stylesheet.render
    val styleElement = dom.document.createElement("style").asInstanceOf[Style]
    styleElement.`type` = "text/css"
    styleElement.innerHTML = rawCssStr
    dom.document.head.appendChild(styleElement)
  }

  private def attachThemeColor(): Unit = {
    val metaElement = dom.document.createElement("meta").asInstanceOf[Meta]
    metaElement.name = "theme-color"
    metaElement.content = MuiTheme.theme.palette.primary.dark
    dom.document.head.appendChild(metaElement)
  }

}
