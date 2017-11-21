package kurtome.dote.web

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import CssSettings._
import kurtome.dote.web.components.widgets.detail.PodcastDetails
import org.scalajs.dom.raw.Event

object WebMain {

  val Hello =
    ScalaComponent
      .builder[String]("Hello")
      .render_P(name => <.div("Hello there ", name))
      .build

  def main(args: Array[String]): Unit = {

    dom.window.onerror = globalErrorHandler

    // Attach both style files to the head
    SharedStyles.addToDocument()
    attachStandaloneStyle(StandaloneStyles)

    val todoappNode =
      dom.document.body.getElementsByClassName("doteapp")(0).domAsHtml

    DoteRoutes.router().renderIntoDOM(todoappNode)
  }

  private def attachStandaloneStyle(stylesheet: StyleSheet.Standalone): Unit = {
    val rawCssStr: String = stylesheet.render
    val styleElement = dom.document.createElement("style")
    styleElement.innerHTML = rawCssStr
    dom.document.head.appendChild(styleElement)
  }

  private def globalErrorHandler(event: Event, source: String, lineno: Int, colno: Int): Boolean = {
    dom.window.alert(s"${event.toString}\nError from '$source' at $lineno:$colno")

    // always suppress the error
    return true;
  }
}
