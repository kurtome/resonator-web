package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui-next.com/api/table-sort-label/
  */
object TableSortLabel {

  @JSImport("material-ui/Table/TableSortLabel", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  @js.native
  trait Props extends js.Object {
    var active: js.UndefOr[Boolean] = js.native
    var direction: js.UndefOr[String] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(active: js.UndefOr[Boolean] = js.undefined,
            dirDescending: Boolean = true,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.active = active
    p.direction = if (dirDescending) "desc" else "asc"
    p.className = className
    component.withProps(p)
  }
}
