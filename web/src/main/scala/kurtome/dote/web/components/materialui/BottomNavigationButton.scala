package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Generic.UnmountedRaw
import japgolly.scalajs.react.raw.ReactElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/bottom-navigation-button/
  */
object BottomNavigationButton {

  @JSImport("material-ui/BottomNavigation/BottomNavigationButton.js", JSImport.Default)
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
    var icon: js.UndefOr[ReactElement] = js.native
    var value: js.UndefOr[String] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(
      value: String,
      onClick: Callback = Callback.empty,
      showLabel: js.UndefOr[Boolean] = js.undefined,
      className: js.UndefOr[String] = js.undefined,
      icon: js.UndefOr[JsComponent.Unmounted[_, _]] = js.undefined
  ) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.onClick = onClick.toJsCallback
    p.showLabel = showLabel
    p.className = className
    p.value = value
    p.icon = icon.map(_.raw)
    component.withProps(p)
  }

}
