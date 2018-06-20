package resonator.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/dialog-content-text/
  */
object DialogContentText {

  @JSImport("@material-ui/core/DialogContentText", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var className: js.UndefOr[String] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(className: js.UndefOr[String] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.className = className
    p.style = style
    component.withProps(p)
  }
}
