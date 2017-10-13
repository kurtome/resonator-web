package kurtome.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/fade/
  */
object Fade {

  @JSImport("material-ui/transitions/Fade.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var in: js.UndefOr[Boolean] = js.native
    var transitionDuration: js.UndefOr[Int] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(in: js.UndefOr[Boolean] = js.undefined,
            transitionDurationMs: js.UndefOr[Int] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.in = in
    p.transitionDuration = transitionDurationMs
    component.withProps(p)
  }
}
