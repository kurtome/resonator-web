package resonator.web.components.materialui

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomNode

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scalacss.internal.StyleA

/**
  * Wrapper for https://material-ui.com/api/text-field/
  */
object TextField {

  @JSImport("@material-ui/core/TextField", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var autoComplete: js.UndefOr[String] = js.native
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
    var label: js.UndefOr[raw.React.Node] = js.native
    var helperText: js.UndefOr[raw.React.Node] = js.native
    var multiline: js.UndefOr[Boolean] = js.native
    var rows: js.UndefOr[Int] = js.native
    var rowsMax: js.UndefOr[Int] = js.native
    var id: js.UndefOr[String] = js.native
    var margin: js.UndefOr[String] = js.native
    var `type`: js.UndefOr[String] = js.native
    var inputRef: js.UndefOr[js.Any] = js.native
    var onChange: js.Function1[ReactEventFromInput, Unit] = js.native
    var onKeyPress: js.Function1[ReactKeyboardEventFromInput, Unit] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(autoComplete: js.UndefOr[String] = js.undefined,
            autoFocus: js.UndefOr[Boolean] = js.undefined,
            fullWidth: js.UndefOr[Boolean] = js.undefined,
            disabled: js.UndefOr[Boolean] = js.undefined,
            required: js.UndefOr[Boolean] = js.undefined,
            error: js.UndefOr[Boolean] = js.undefined,
            value: js.UndefOr[String] = js.undefined,
            name: js.UndefOr[String] = js.undefined,
            placeholder: js.UndefOr[String] = js.undefined,
            label: js.UndefOr[VdomNode] = js.undefined,
            multiline: js.UndefOr[Boolean] = js.undefined,
            rows: js.UndefOr[Int] = js.undefined,
            rowsMax: js.UndefOr[Int] = js.undefined,
            helperText: js.UndefOr[VdomNode] = js.undefined,
            inputType: js.UndefOr[String] = js.undefined,
            inputRef: js.UndefOr[js.Any] = js.undefined,
            onChange: ReactEventFromInput => Callback = _ => Callback.empty,
            onKeyPress: ReactKeyboardEventFromInput => Callback = _ => Callback.empty,
            className: js.UndefOr[String] = js.undefined,
            margin: String = "normal") = {
    val p = (new js.Object).asInstanceOf[Props]
    p.autoComplete = autoComplete
    p.value = value
    p.name = name
    p.autoFocus = autoFocus
    p.disabled = disabled
    p.required = required
    p.error = error
    p.placeholder = placeholder
    p.label = label.map(_.rawNode)
    p.multiline = multiline
    p.rows = rows
    p.rowsMax = rowsMax
    p.helperText = helperText.map(_.rawNode)
    p.className = className
    p.margin = margin
    p.fullWidth = fullWidth
    p.`type` = inputType
    p.inputRef = inputRef
    p.onChange = (e) => onChange(e).runNow()
    p.onKeyPress = (e) => onKeyPress(e).runNow()

    component.withProps(p)
  }

}
