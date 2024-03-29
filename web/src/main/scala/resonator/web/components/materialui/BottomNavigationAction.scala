package resonator.web.components.materialui

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Generic.{Unmounted, UnmountedSimple}
import japgolly.scalajs.react.vdom._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui.com/api/bottom-navigation-action/
  */
object BottomNavigationAction {

  @JSImport("@material-ui/core/BottomNavigationAction", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var showLabel: js.UndefOr[Boolean] = js.native
    var button: js.UndefOr[Boolean] = js.native
    var disabled: js.UndefOr[Boolean] = js.native
    var disableRipple: js.UndefOr[Boolean] = js.native
    var focusRipple: js.UndefOr[Boolean] = js.native
    var centerRipple: js.UndefOr[Boolean] = js.native
    var onClick: js.UndefOr[js.Function0[Unit]] = js.native
    var icon: js.UndefOr[raw.React.Element] = js.native
    var label: js.UndefOr[raw.React.Element] = js.native
    var value: js.UndefOr[String] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(
      value: String,
      onClick: Callback = Callback.empty,
      showLabel: js.UndefOr[Boolean] = js.undefined,
      className: js.UndefOr[String] = js.undefined,
      label: js.UndefOr[GenericComponent.Unmounted[_, _]] = js.undefined,
      icon: js.UndefOr[GenericComponent.Unmounted[_, _]] = js.undefined
  ) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.onClick = onClick.toJsCallback
    p.showLabel = showLabel
    p.className = className
    p.value = value
    p.label = label.map(_.raw)
    p.icon = icon.map(_.raw)
    component.withProps(p)
  }

}
