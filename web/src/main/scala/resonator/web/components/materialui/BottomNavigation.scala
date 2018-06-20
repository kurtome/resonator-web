package resonator.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui.com/api/bottom-navigation/
  */
object BottomNavigation {

  @JSImport("@material-ui/core/BottomNavigation", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var onChange: js.Function2[js.Dynamic, String, Unit] = js.native
    var showLabels: js.UndefOr[Boolean] = js.native
    var value: js.UndefOr[String] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(
      onChange: (js.Dynamic, String) => Callback = (_, _) => Callback.empty,
      showLabels: js.UndefOr[Boolean] = js.undefined,
      value: js.UndefOr[String] = js.undefined,
      style: js.UndefOr[js.Dynamic] = js.undefined
  ) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.onChange = (e, v) => onChange(e, v).runNow()
    p.showLabels = showLabels
    p.style = style
    p.value = value
    component.withProps(p)
  }
}
