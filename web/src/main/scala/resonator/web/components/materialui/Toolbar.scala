package resonator.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/toolbar/
  */
object Toolbar {

  @JSImport("@material-ui/core/Toolbar", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var disableGutters: js.UndefOr[Boolean] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(disableGutters: js.UndefOr[Boolean] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.disableGutters = disableGutters
    p.style = style

    component.withProps(p)
  }

}
