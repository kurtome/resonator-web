package resonator.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/list/
  */
object List {

  @JSImport("@material-ui/core/List", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var dense: js.UndefOr[Boolean] = js.native
    var disablePadding: js.UndefOr[Boolean] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(dense: js.UndefOr[Boolean] = js.undefined,
            disablePadding: js.UndefOr[Boolean] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.dense = dense
    p.disablePadding = disablePadding
    p.style = style

    component.withProps(p)
  }

}
