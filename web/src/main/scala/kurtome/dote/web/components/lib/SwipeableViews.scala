package kurtome.dote.web.components.lib

import japgolly.scalajs.react
import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://react-swipeable-views.com
  */
object SwipeableViews {

  @JSImport("react-swipeable-views", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Axes extends Enumeration {
    val X = Value("x")
    val XReverse = Value("x-reverse")
    val Y = Value("y")
    val YReverse = Value("y-reverse")
  }
  type Axis = Axes.Value

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var axis: js.UndefOr[String] = js.native
    var index: js.UndefOr[Int] = js.native

    /**
      * function(index, indexLatest, meta)
      */
    var onChangeIndex: js.UndefOr[js.Function3[Int, Int, js.Dynamic, Unit]] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(
      axis: js.UndefOr[Axis] = js.undefined,
      index: js.UndefOr[Int] = js.undefined,
      onIndexChanged: js.UndefOr[js.Function3[Int, Int, js.Dynamic, Unit]] = js.undefined,
  ) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.axis = axis.map(_.toString)
    p.index = index
    p.onChangeIndex = p.onChangeIndex

    component.withProps(p)
  }
}