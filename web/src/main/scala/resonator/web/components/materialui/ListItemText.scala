package resonator.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/list-item-text/
  */
object ListItemText {

  @JSImport("@material-ui/core/ListItemText", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var disableTypography: js.UndefOr[Boolean] = js.native
    var inset: js.UndefOr[Boolean] = js.native
    var primary: js.UndefOr[String] = js.native
    var secondary: js.UndefOr[String] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(disableTypography: js.UndefOr[Boolean] = js.undefined,
            inset: js.UndefOr[Boolean] = js.undefined,
            primary: js.UndefOr[String] = js.undefined,
            secondary: js.UndefOr[String] = js.undefined,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.disableTypography = disableTypography
    p.inset = inset
    p.primary = primary
    p.secondary = secondary
    p.className = className

    component.withProps(p)
  }

}
