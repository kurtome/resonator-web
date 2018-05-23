package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui.com/api/fade/
  */
object Fade {

  @JSImport("@material-ui/core/Fade", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var in: js.UndefOr[Boolean] = js.native
    var appear: js.UndefOr[Boolean] = js.native
    var timeout: js.UndefOr[Int] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  @js.native
  trait State extends js.Object {}

  val component = JsComponent[Props, Children.Varargs, State](RawComponent)

  def apply(in: js.UndefOr[Boolean] = js.undefined,
            appear: js.UndefOr[Boolean] = js.undefined,
            timeoutMs: js.UndefOr[Int] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.in = in
    p.appear = appear
    p.timeout = timeoutMs
    p.style = style
    component.withProps(p)
  }
}
