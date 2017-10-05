package kurtome.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object HelloView {

  val component =
    ScalaComponent
      .builder[String]("Hello")
      .render_P(name => <.div("Hello there ", name))
      .build

}
