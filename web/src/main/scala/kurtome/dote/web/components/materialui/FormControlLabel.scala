package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-next.com/api/form-control-label/
  */
object FormControlLabel {

  @JSImport("material-ui/Form/FormControlLabel", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var control: js.UndefOr[raw.ReactElement] = js.native
    var label: js.UndefOr[raw.ReactElement] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(
      control: js.UndefOr[GenericComponent.Unmounted[_, _]] = js.undefined,
      label: js.UndefOr[GenericComponent.Unmounted[_, _]] = js.undefined
  ) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.control = control.map(_.raw)
    p.label = label.map(_.raw)
    component.withProps(p)
  }

}
