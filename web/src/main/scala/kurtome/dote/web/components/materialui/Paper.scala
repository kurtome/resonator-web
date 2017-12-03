package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._
import kurtome.dote.web.components.ComponentHelpers

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scalacss.internal.StyleA

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/paper/
  */
object Paper {

  @JSImport("material-ui/Paper/Paper.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var className: js.UndefOr[String] = js.native
    var elevation: js.UndefOr[Int] = js.native
    var square: js.UndefOr[Boolean] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(baseProps: js.Any = new js.Object,
            elevation: js.UndefOr[Int] = js.undefined,
            className: js.UndefOr[String] = js.undefined,
            square: js.UndefOr[Boolean] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = baseProps.asInstanceOf[Props]
    p.elevation = elevation
    p.square = square
    p.className = className
    if (p.style.isDefined && style.isDefined) {
      p.style = ComponentHelpers.mergeJSObjects(p.style.get, style.get)
    } else if (style.isDefined) {
      p.style = style.get
    }

    component.withProps(p)
  }

}
