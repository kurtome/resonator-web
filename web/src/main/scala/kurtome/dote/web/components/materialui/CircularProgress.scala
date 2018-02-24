package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-next.com/api/circular-progress/
  */
object CircularProgress {

  @JSImport("material-ui/Progress/CircularProgress.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Color extends Enumeration {
    val Primary = Value("primary")
    val Secondary = Value("secondary")
    val Inherit = Value("inherit")
  }

  object Variant extends Enumeration {
    val Determinate = Value("determinate")
    val Indeterminate = Value("indeterminate")
    val Static = Value("static")
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var color: js.UndefOr[String] = js.native
    var variant: js.UndefOr[String] = js.native
    var value: js.UndefOr[Float] = js.native
    var thickness: js.UndefOr[Float] = js.native
    var min: js.UndefOr[Float] = js.native
    var max: js.UndefOr[Float] = js.native
    var size: js.UndefOr[Float] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(color: js.UndefOr[Color.Value] = js.undefined,
            variant: js.UndefOr[Variant.Value] = js.undefined,
            value: js.UndefOr[Float] = js.undefined,
            thickness: js.UndefOr[Float] = js.undefined,
            min: js.UndefOr[Float] = js.undefined,
            max: js.UndefOr[Float] = js.undefined,
            size: js.UndefOr[Float] = js.undefined,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.color = color.map(_.toString)
    p.variant = variant.map(_.toString)
    p.value = value
    p.thickness = thickness
    p.min = min
    p.max = max
    p.size = size
    p.className = className

    component.withProps(p)
  }
}
