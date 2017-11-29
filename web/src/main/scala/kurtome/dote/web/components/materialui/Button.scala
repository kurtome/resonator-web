package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._
import kurtome.dote.web.SharedStyles

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scalacss.internal.StyleA

/**
  * Wrapper for https://material-ui-1dab0.firebaseapp.com/api/button/
  */
object Button {

  @JSImport("material-ui/Button/Button.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Color extends Enumeration {
    val Default = Value("default")
    val Primary = Value("primary")
    val Inherit = Value("inherit")
    val Accent = Value("accent")
    val Contrast = Value("contrast")
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
    var raised: js.UndefOr[Boolean] = js.native
    var onClick: js.Function0[Unit] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(color: js.UndefOr[Color.Value] = js.undefined,
            className: js.UndefOr[String] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined,
            raised: js.UndefOr[Boolean] = js.undefined,
            disabled: js.UndefOr[Boolean] = js.undefined,
            disableRipple: js.UndefOr[Boolean] = js.undefined,
            disableFocusRipple: js.UndefOr[Boolean] = js.undefined,
            onClick: Callback = Callback.empty) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.color = color map { _.toString }
    p.className = className
    p.style = style
    p.onClick = onClick.toJsFn
    p.disabled = disabled
    p.disableRipple = disableRipple
    p.disableFocusRipple = disableFocusRipple
    p.raised = raised

    component.withProps(p)
  }

}
