package kurtome.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/linear-progress/
  */
object LinearProgress {

  @JSImport("material-ui/Progress/LinearProgress.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Color extends Enumeration {
    val Primary = Value("primary")
    val Accent = Value("accent")
  }

  object Mode extends Enumeration {
    val Determinate = Value("determinate")
    val Indeterminate = Value("indeterminate")
    val Buffer = Value("buffer")
    val Query = Value("query")
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var color: js.UndefOr[String] = js.native
    var mode: js.UndefOr[String] = js.native
    var value: js.UndefOr[Int] = js.native
    var valueBuffer: js.UndefOr[Int] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(color: js.UndefOr[Color.Value] = js.undefined,
            mode: js.UndefOr[Mode.Value] = js.undefined,
            value: js.UndefOr[Int] = js.undefined,
            valueBuffer: js.UndefOr[Int] = js.undefined,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.color = color.map(_.toString)
    p.mode = mode.map(_.toString)
    p.value = value
    p.valueBuffer = valueBuffer
    p.className = className

    component.withProps(p)
  }
}
