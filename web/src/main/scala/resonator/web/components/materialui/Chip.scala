package resonator.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui.com/api/chip/
  */
object Chip {

  @JSImport("@material-ui/core/Chip", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var label: js.UndefOr[raw.React.Element] = js.native
    var onClick: js.Function0[Unit] = js.native
    var onDelete: js.Function0[Unit] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(label: js.UndefOr[GenericComponent.Unmounted[_, _]] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined,
            onDelete: Callback = Callback.empty,
            onClick: Callback = Callback.empty) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.label = label.map(_.raw)
    if (onDelete != Callback.empty) {
      p.onDelete = onDelete.toJsFn
    }
    p.onClick = onClick.toJsFn
    p.style = style
    component.withProps(p)
  }
}
