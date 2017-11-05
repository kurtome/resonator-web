package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/divider/
  */
object Divider {

  @JSImport("material-ui/Divider/Divider.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var inset: js.UndefOr[Boolean] = js.native
    var light: js.UndefOr[Boolean] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(inset: js.UndefOr[Boolean] = js.undefined, light: js.UndefOr[Boolean] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.inset = inset
    p.light = light
    component.withProps(p)
  }
}
