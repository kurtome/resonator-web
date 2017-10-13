package kurtome.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scalacss.internal.ClassName

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
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(elevation: js.UndefOr[Int] = js.undefined,
            className: js.UndefOr[ClassName] = js.undefined,
            square: js.UndefOr[Boolean] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.elevation = elevation
    p.square = square
    p.className = className map { _.value }

    component.withProps(p)
  }

}
