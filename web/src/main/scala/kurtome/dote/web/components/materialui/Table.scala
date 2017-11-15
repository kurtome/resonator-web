package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui-next.com/api/table/
  */
object Table {

  @JSImport("material-ui/Table/Table", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  @js.native
  trait Props extends js.Object {
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.className = className
    component.withProps(p)
  }
}
