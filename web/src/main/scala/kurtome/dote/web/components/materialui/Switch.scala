package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/switch/
  */
object Switch {

  @JSImport("@material-ui/core/Switch", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Colors extends Enumeration {
    val Primary = Value("primary")
    val Secondary = Value("secondary")
    val Default = Value("default")
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var checked: js.UndefOr[Boolean] = js.native
    var color: js.UndefOr[String] = js.native
    var disabled: js.UndefOr[Boolean] = js.native
    var disableRipple: js.UndefOr[Boolean] = js.native
    var value: js.UndefOr[String] = js.native
    var onChange: js.Function1[ReactEventFromInput, Unit] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(
      checked: js.UndefOr[Boolean] = js.undefined,
      color: js.UndefOr[String] = js.undefined,
      disabled: js.UndefOr[Boolean] = js.undefined,
      disableRipple: js.UndefOr[Boolean] = js.undefined,
      value: js.UndefOr[String] = js.undefined,
      onChange: (ReactEventFromInput) => Callback = _ => Callback.empty,
      style: js.UndefOr[js.Dynamic] = js.undefined,
  ) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.checked = checked
    p.color = color
    p.disabled = disabled
    p.disableRipple = disableRipple
    p.value = value
    p.onChange = (e) => onChange(e).runNow()
    p.style = style

    component.withProps(p)
  }

}
