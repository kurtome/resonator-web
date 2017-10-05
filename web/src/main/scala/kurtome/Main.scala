package kurtome

import dote.proto.addpodcast.AddPodcastRequest
import kurtome.api.DoteProtoApi

import scala.concurrent.ExecutionContext.Implicits.global
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

object Main {

  val Hello =
    ScalaComponent
      .builder[String]("Hello")
      .render_P(name => <.div("Hello there ", name))
      .build

  def main(args: Array[String]): Unit = {
    val todoappNode =
      dom.document.body.getElementsByClassName("doteapp")(0).domAsHtml
    DoteRoutes.router().renderIntoDOM(todoappNode)

    HelloDummy.runApiRequests

    DoteProtoApi.addPodcast(
      AddPodcastRequest("http://feeds.gimletmedia.com/hearreplyall")) map {
      response =>
        println(response.toString)
    }
  }
}
