package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui.com/api/app-bar/
  */
object AppBar {

  @JSImport("@material-ui/core/AppBar", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Position extends Enumeration {
    val Fixed = Value("fixed")
    val Static = Value("static")
    val Absolute = Value("absolute")
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var position: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(position: js.UndefOr[Position.Value] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.position = position.map(_.toString)
    component.withProps(p)
  }
}
