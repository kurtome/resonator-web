package resonator.web.components.materialui

import japgolly.scalajs.react
import japgolly.scalajs.react._
import resonator.web.components.materialui.Dialog.MaxWidths.MaxWidth

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.JSConverters._

/**
  * Wrapper for https://material-ui.com/api/dialog/
  */
object Dialog {

  @JSImport("@material-ui/core/Dialog", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object MaxWidths extends Enumeration {
    type MaxWidth = Value
    val Xs = Value("xs")
    val Sm = Value("sm")
    val Md = Value("md")
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var className: js.UndefOr[String] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
    var open: js.UndefOr[Boolean] = js.native
    var disableBackdropClick: js.UndefOr[Boolean] = js.native
    var disableEscapeKeyDown: js.UndefOr[Boolean] = js.native
    var fullScreen: js.UndefOr[Boolean] = js.native
    var fullWidth: js.UndefOr[Boolean] = js.native
    var maxWidth: js.UndefOr[String] = js.native
    var onBackdropClick: js.Function0[Unit] = js.native
    var onClose: js.Function0[Unit] = js.native
    var onEscapeKeyDown: js.UndefOr[js.Function0[Unit]] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(className: js.UndefOr[String] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined,
            open: js.UndefOr[Boolean] = js.undefined,
            disableBackdropClick: js.UndefOr[Boolean] = js.undefined,
            disableEscapeKeyDown: js.UndefOr[Boolean] = js.undefined,
            fullScreen: js.UndefOr[Boolean] = js.undefined,
            fullWidth: js.UndefOr[Boolean] = js.undefined,
            maxWidth: js.UndefOr[MaxWidth] = js.undefined,
            onBackdropClick: Callback = Callback.empty,
            onClose: Callback = Callback.empty,
            onEscapeKeyDown: Callback = Callback.empty) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.className = className
    p.style = style
    p.open = open
    p.disableBackdropClick = disableBackdropClick
    p.disableEscapeKeyDown = disableEscapeKeyDown
    p.fullScreen = fullScreen
    p.fullWidth = fullWidth
    p.maxWidth = maxWidth.map(_.toString)
    p.onBackdropClick = onBackdropClick.toJsFn
    p.onClose = onClose.toJsFn
    p.onEscapeKeyDown = onEscapeKeyDown.toJsFn
    component.withProps(p)
  }
}
