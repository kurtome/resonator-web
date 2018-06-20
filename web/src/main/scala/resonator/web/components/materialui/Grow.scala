package resonator.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/grow/
  */
object Grow {

  @JSImport("@material-ui/core/Grow", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var in: js.UndefOr[Boolean] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val jsComponent = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(in: js.UndefOr[Boolean] = js.undefined, style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.in = in
    p.style = style

    jsComponent.withProps(p)
  }
}
