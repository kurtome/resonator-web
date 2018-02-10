package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-next.com/api/checkbox/
  */
object Checkbox {

  @JSImport("material-ui/Checkbox", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var checked: js.UndefOr[Boolean] = js.native
    var checkedIcon: js.UndefOr[raw.ReactElement] = js.native
    var disabled: js.UndefOr[Boolean] = js.native
    var icon: js.UndefOr[raw.ReactElement] = js.native
    var indeterminate: js.UndefOr[Boolean] = js.native
    var indeterminateIcon: js.UndefOr[raw.ReactElement] = js.native
    var name: js.UndefOr[String] = js.native
    var value: js.UndefOr[String] = js.native
    var onChange: js.Function1[ReactEventFromInput, Unit] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(
      checked: js.UndefOr[Boolean] = js.undefined,
      checkedIcon: js.UndefOr[raw.ReactElement] = js.undefined,
      disabled: js.UndefOr[Boolean] = js.undefined,
      icon: js.UndefOr[raw.ReactElement] = js.undefined,
      indeterminate: js.UndefOr[Boolean] = js.undefined,
      indeterminateIcon: js.UndefOr[raw.ReactElement] = js.undefined,
      name: js.UndefOr[String] = js.undefined,
      value: js.UndefOr[String] = js.undefined,
      onChange: (ReactEventFromInput) => Callback = _ => Callback.empty
  ) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.checked = checked
    p.checkedIcon = checkedIcon
    p.disabled = disabled
    p.icon = icon
    p.indeterminate = indeterminate
    p.indeterminateIcon = indeterminateIcon
    p.name = name
    p.value = value
    p.onChange = (e) => onChange(e).runNow()

    component.withProps(p)
  }

}
