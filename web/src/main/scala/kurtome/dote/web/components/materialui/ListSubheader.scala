package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/list-subheader/
  */
object ListSubheader {

  @JSImport("@material-ui/core/ListSubheader", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Color extends Enumeration {
    val Default = Value("default")
    val Primary = Value("primary")
    val Inherit = Value("inherit")
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var color: js.UndefOr[String] = js.native
    var disableSticky: js.UndefOr[Boolean] = js.native
    var inset: js.UndefOr[Boolean] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(color: js.UndefOr[Color.Value] = js.undefined,
            disableSticky: js.UndefOr[Boolean] = js.undefined,
            inset: js.UndefOr[Boolean] = js.undefined,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.color = color map { _.toString }
    p.disableSticky = disableSticky
    p.inset = inset
    p.className = className

    component.withProps(p)
  }

}
