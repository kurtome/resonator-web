package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.materialui.Button.Colors.Color
import kurtome.dote.web.components.materialui.Button.Variants.Variant

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scalacss.internal.StyleA

/**
  * Wrapper for https://material-ui.com/api/button/
  */
object Button {

  @JSImport("@material-ui/core/Button", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Colors extends Enumeration {
    type Color = Value
    val Default = Value("default")
    val Primary = Value("primary")
    val Secondary = Value("secondary")
    val Inherit = Value("inherit")
  }

  object Variants extends Enumeration {
    type Variant = Value
    val Flat = Value("flat")
    val Raised = Value("raised")
    val Floating = Value("fab")
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var color: js.UndefOr[String] = js.native
    var className: js.UndefOr[String] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
    var disabled: js.UndefOr[Boolean] = js.native
    var disableRipple: js.UndefOr[Boolean] = js.native
    var disableFocusRipple: js.UndefOr[Boolean] = js.native
    var variant: js.UndefOr[String] = js.native
    var onClick: js.Function0[Unit] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(color: js.UndefOr[Color] = js.undefined,
            className: js.UndefOr[String] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined,
            variant: js.UndefOr[Variant] = js.undefined,
            disabled: js.UndefOr[Boolean] = js.undefined,
            disableRipple: js.UndefOr[Boolean] = js.undefined,
            disableFocusRipple: js.UndefOr[Boolean] = js.undefined,
            onClick: Callback = Callback.empty) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.color = color.map(_.toString)
    p.className = className
    p.style = style
    p.onClick = onClick.toJsFn
    p.disabled = disabled
    p.disableRipple = disableRipple
    p.disableFocusRipple = disableFocusRipple
    p.variant = variant.map(_.toString)

    component.withProps(p)
  }

}
