package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/list-item/
  */
object ListItem {

  @JSImport("@material-ui/core/ListItem", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var dense: js.UndefOr[Boolean] = js.native
    var disableGutters: js.UndefOr[Boolean] = js.native
    var divider: js.UndefOr[Boolean] = js.native
    var button: js.UndefOr[Boolean] = js.native
    var onClick: js.Function0[Unit] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(dense: js.UndefOr[Boolean] = js.undefined,
            disableGutters: js.UndefOr[Boolean] = js.undefined,
            divider: js.UndefOr[Boolean] = js.undefined,
            button: js.UndefOr[Boolean] = js.undefined,
            onClick: Callback = Callback.empty,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.dense = dense
    p.disableGutters = disableGutters
    p.style = style
    p.onClick = onClick.toJsFn
    p.divider = divider
    p.button = button

    component.withProps(p)
  }

}
