package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui-next.com/api/backdrop/
  */
object Backdrop {

  @JSImport("material-ui/internal/Backdrop", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var invisible: js.UndefOr[Boolean] = js.native
    var open: js.UndefOr[Boolean] = js.native
    var transitionDuration: js.UndefOr[Float] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(invisible: js.UndefOr[Boolean] = js.undefined,
            open: js.UndefOr[Boolean] = js.undefined,
            transitionDuration: js.UndefOr[Float] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.invisible = invisible
    p.open = open
    p.transitionDuration = transitionDuration
    p.style = style
    component.withProps(p)
  }
}
