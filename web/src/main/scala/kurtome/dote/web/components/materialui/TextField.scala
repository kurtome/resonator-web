package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scalacss.internal.StyleA

/**
  * Wrapper for https://material-ui-next.com/api/text-field/
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
    var required: js.UndefOr[Boolean] = js.native
    var error: js.UndefOr[Boolean] = js.native
    var fullWidth: js.UndefOr[Boolean] = js.native
    var placeholder: js.UndefOr[String] = js.native
    var label: js.UndefOr[raw.ReactElement] = js.native
    var helperText: js.UndefOr[raw.ReactElement] = js.native
    var id: js.UndefOr[String] = js.native
    var margin: js.UndefOr[String] = js.native
    var `type`: js.UndefOr[String] = js.native
    var inputRef: js.UndefOr[js.Any] = js.native
    var onChange: js.Function1[ReactEventFromInput, Unit] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(autoFocus: js.UndefOr[Boolean] = js.undefined,
            fullWidth: js.UndefOr[Boolean] = js.undefined,
            disabled: js.UndefOr[Boolean] = js.undefined,
            required: js.UndefOr[Boolean] = js.undefined,
            error: js.UndefOr[Boolean] = js.undefined,
            value: js.UndefOr[String] = js.undefined,
            name: js.UndefOr[String] = js.undefined,
            placeholder: js.UndefOr[String] = js.undefined,
            label: js.UndefOr[GenericComponent.Unmounted[_, _]] = js.undefined,
            helperText: js.UndefOr[GenericComponent.Unmounted[_, _]] = js.undefined,
            inputType: js.UndefOr[String] = js.undefined,
            inputRef: js.UndefOr[js.Any] = js.undefined,
            onChange: ReactEventFromInput => Callback = _ => Callback.empty,
            className: js.UndefOr[String] = js.undefined,
            margin: String = "normal") = {
    val p = (new js.Object).asInstanceOf[Props]
    p.value = value
    p.name = name
    p.autoFocus = autoFocus
    p.disabled = disabled
    p.required = required
    p.error = error
    p.placeholder = placeholder
    p.label = label.map(_.raw)
    p.helperText = helperText.map(_.raw)
    p.className = className
    p.margin = margin
    p.fullWidth = fullWidth
    p.`type` = inputType
    p.inputRef = inputRef
    p.onChange = (s: ReactEventFromInput) => onChange(s).runNow()

    component.withProps(p)
  }

}
