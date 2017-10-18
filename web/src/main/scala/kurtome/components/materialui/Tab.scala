package kurtome.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/tab/
  */
object Tab {

  @JSImport("material-ui/Tabs/Tab.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var label: js.UndefOr[String] = js.native
    var disabled: js.UndefOr[Boolean] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(label: js.UndefOr[String] = js.undefined,
            disabled: js.UndefOr[Boolean] = js.undefined,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.label = label
    p.disabled = disabled
    p.className = className

    component.withProps(p)
  }
}
