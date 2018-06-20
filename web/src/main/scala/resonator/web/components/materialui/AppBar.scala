package resonator.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui.com/api/app-bar/
  */
object AppBar {

  @JSImport("@material-ui/core/AppBar", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Positions extends Enumeration {
    val Fixed = Value("fixed")
    val Static = Value("static")
    val Sticky = Value("sticky")
    val Absolute = Value("absolute")
  }
  type Position = Positions.Value

  object Colors extends Enumeration {
    val Primary = Value("primary")
    val Secondary = Value("secondary")
    val Inherit = Value("inherit")
    val Default = Value("default")
  }
  type Color = Colors.Value

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var position: js.UndefOr[String] = js.native
    var color: js.UndefOr[String] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(
      position: js.UndefOr[Position] = js.undefined,
      color: js.UndefOr[Color] = js.undefined,
      style: js.UndefOr[js.Dynamic] = js.undefined
  ) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.position = position.map(_.toString)
    p.color = color.map(_.toString)
    p.style = style
    component.withProps(p)
  }
}
