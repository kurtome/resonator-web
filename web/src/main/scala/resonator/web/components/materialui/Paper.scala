package resonator.web.components.materialui

import japgolly.scalajs.react._
import resonator.web.components.ComponentHelpers

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scalacss.internal.StyleA

/**
  * Wrapper for https://material-ui.com/api/paper/
  */
object Paper {

  @JSImport("@material-ui/core/Paper", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var elevation: js.UndefOr[Int] = js.native
    var square: js.UndefOr[Boolean] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }
  object Props {
    def apply(baseProps: js.Any = new js.Object,
              elevation: js.UndefOr[Int] = js.undefined,
              square: js.UndefOr[Boolean] = js.undefined,
              style: js.UndefOr[js.Dynamic] = js.undefined): Props = {
      val p = baseProps.asInstanceOf[Props]
      p.elevation = elevation
      p.square = square
      if (p.style.isDefined && style.isDefined) {
        p.style = ComponentHelpers.mergeJSObjects(p.style.get, style.get)
      } else if (style.isDefined) {
        p.style = style.get
      }
      p
    }
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(baseProps: js.Any = new js.Object,
            elevation: js.UndefOr[Int] = js.undefined,
            square: js.UndefOr[Boolean] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = baseProps.asInstanceOf[Props]
    p.elevation = elevation
    p.square = square
    if (p.style.isDefined && style.isDefined) {
      p.style = ComponentHelpers.mergeJSObjects(p.style.get, style.get)
    } else if (style.isDefined) {
      p.style = style.get
    }

    component.withProps(p)
  }

}
