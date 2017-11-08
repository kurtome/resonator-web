package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/bottom-navigation/
  */
object BottomNavigation {

  @JSImport("material-ui/BottomNavigation/BottomNavigation.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var onChange: js.Function2[js.Dynamic, Int, Unit] = js.native
    var showLabels: js.UndefOr[Boolean] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(
      onChange: (js.Dynamic, Int) => Callback = (_, _) => Callback.empty,
      showLabels: js.UndefOr[Boolean] = js.undefined,
      className: js.UndefOr[String] = js.undefined
  ) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.onChange = (e, v) => onChange(e, v).runNow()
    p.showLabels = showLabels
    p.className = className
    component.withProps(p)
  }
}
