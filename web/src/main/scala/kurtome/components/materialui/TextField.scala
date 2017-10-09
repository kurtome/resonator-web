package kurtome.components.materialui

import japgolly.scalajs.react._
import kurtome.Styles

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/text-field/
  */
object TextField {

  @JSImport("material-ui/TextField/TextField.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var autoFocus: js.UndefOr[Boolean] = js.native
    var name: js.UndefOr[String] = js.native
    var value: js.UndefOr[String] = js.native
    var className: js.UndefOr[String] = js.native
    var defaultValue: js.UndefOr[String] = js.native
    var disabled: js.UndefOr[Boolean] = js.native
    var error: js.UndefOr[Boolean] = js.native
    var fullWidth: js.UndefOr[Boolean] = js.native
    var placeholder: js.UndefOr[String] = js.native
    var label: js.UndefOr[String] = js.native
    var helperText: js.UndefOr[String] = js.native
    var id: js.UndefOr[String] = js.native
    var margin: js.UndefOr[String] = js.native
    var `type`: js.UndefOr[String] = js.native
    var onChange: js.Function1[ReactEventFromInput, Unit] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(autoFocus: js.UndefOr[Boolean] = js.undefined,
            fullWidth: js.UndefOr[Boolean] = js.undefined,
            value: js.UndefOr[String] = js.undefined,
            name: js.UndefOr[String] = js.undefined,
            placeholder: js.UndefOr[String] = js.undefined,
            label: js.UndefOr[String] = js.undefined,
            helperText: js.UndefOr[String] = js.undefined,
            onChange: ReactEventFromInput => Callback = _ => Callback.empty,
            className: js.UndefOr[String] = Styles.textField.className.value,
            margin: String = "normal") = {
    val p = (new js.Object).asInstanceOf[Props]
    p.value = value
    p.name = name
    p.autoFocus = autoFocus
    p.placeholder = placeholder
    p.label = label
    p.helperText = helperText
    p.className = className
    p.margin = margin
    p.fullWidth = fullWidth
    p.onChange = (s: ReactEventFromInput) => onChange(s).runNow()

    component.withProps(p)
  }

}
