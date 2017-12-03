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

}
