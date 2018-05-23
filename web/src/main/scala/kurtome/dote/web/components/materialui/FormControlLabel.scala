package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/form-control-label/
  */
object FormControlLabel {

  @JSImport("@material-ui/core/FormControlLabel", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var control: js.UndefOr[raw.React.Node] = js.native
    var label: js.UndefOr[raw.React.Node] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(control: vdom.VdomNode, label: vdom.VdomNode) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.control = control.rawNode
    p.label = label.rawNode
    component.withProps(p)
  }

}
