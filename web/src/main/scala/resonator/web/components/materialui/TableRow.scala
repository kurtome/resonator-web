package resonator.web.components.materialui

import japgolly.scalajs.react
import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://material-ui.com/api/table-row/
  */
object TableRow {

  @JSImport("@material-ui/core/TableRow", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  @js.native
  trait Props extends js.Object {
    var hover: js.UndefOr[Boolean] = js.native
    var selected: js.UndefOr[Boolean] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(key: Option[react.Key] = None,
            hover: js.UndefOr[Boolean] = js.undefined,
            selected: js.UndefOr[Boolean] = js.undefined,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.hover = hover
    p.selected = selected
    p.className = className

    if (key.isDefined) {
      component.withKey(key.get).withProps(p)
    } else {
      component.withProps(p)
    }
  }
}
