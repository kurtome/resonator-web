package kurtome.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/tabs/
  */
object Tabs {

  @JSImport("material-ui/Tabs/Tabs.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object IndicatorColor extends Enumeration {
    val Accent = Value("accent")
    val Primary = Value("primary")
  }

  object TextColor extends Enumeration {
    val Accent = Value("accent")
    val Primary = Value("primary")
    val Inherit = Value("inherit")
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var buttonClassName: js.UndefOr[String] = js.native
    var centered: js.UndefOr[Boolean] = js.native
    var fullWidth: js.UndefOr[Boolean] = js.native
    var indicatorClassName: js.UndefOr[String] = js.native
    var scrollable: js.UndefOr[Boolean] = js.native
    var value: js.UndefOr[Int] = js.native
    var onChange: js.Function2[js.Dynamic, Int, Unit] = js.native
    var className: js.UndefOr[String] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(value: js.UndefOr[Int],
            buttonClassName: js.UndefOr[String] = js.undefined,
            centered: js.UndefOr[Boolean] = js.undefined,
            fullWidth: js.UndefOr[Boolean] = js.undefined,
            indicatorClassName: js.UndefOr[String] = js.undefined,
            scrollable: js.UndefOr[Boolean] = js.undefined,
            onChange: (js.Dynamic, Int) => Callback = (_, _) => Callback.empty,
            className: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.buttonClassName = buttonClassName
    p.centered = centered
    p.fullWidth = fullWidth
    p.indicatorClassName = indicatorClassName
    p.scrollable = scrollable
    p.onChange = (e: js.Dynamic, v: Int) => onChange(e, v).runNow()
    p.className = className
    p.value = value

    component.withProps(p)
  }
}
