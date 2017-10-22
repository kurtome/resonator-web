package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/list-item/
  */
object ListItem {

  @JSImport("material-ui/List/ListItem.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var dense: js.UndefOr[Boolean] = js.native
    var disableGutters: js.UndefOr[Boolean] = js.native
    var divider: js.UndefOr[Boolean] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(dense: js.UndefOr[Boolean] = js.undefined,
            disableGutters: js.UndefOr[Boolean] = js.undefined,
            divider: js.UndefOr[Boolean] = js.undefined,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.dense = dense
    p.disableGutters = disableGutters
    p.className = className

    component.withProps(p)
  }

}
