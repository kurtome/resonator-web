package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui-next.com/api/table-cell/
  */
object TableCell {

  @JSImport("material-ui/Table/TableCell", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Padding extends Enumeration {
    val Default = Value("default")
    val Checkbox = Value("checkbox")
    val Dense = Value("dense")
    val None = Value("none")
  }

  @js.native
  trait Props extends js.Object {
    var padding: js.UndefOr[String] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(padding: js.UndefOr[Padding.Value] = js.undefined,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.padding = padding.map(_.toString)
    p.className = className
    component.withProps(p)
  }
}
