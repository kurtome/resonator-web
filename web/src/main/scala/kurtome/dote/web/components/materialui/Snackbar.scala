package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/demos/snackbars/
  */
object Snackbar {

  @JSImport("@material-ui/core/Snackbar", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Color extends Enumeration {
    val Default = Value("default")
    val Primary = Value("primary")
    val Inherit = Value("inherit")
    val Accent = Value("accent")
    val Contrast = Value("contrast")
  }

  @js.native
  trait Shape extends js.Object {
    var vertical: String = js.native
    var horizontal: String = js.native
  }
  object Shape {
    def apply(vertical: String, horizontal: String): Shape = {
      val shape = (new js.Object).asInstanceOf[Shape]
      shape.vertical = vertical
      shape.horizontal = horizontal
      shape
    }
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var action: js.UndefOr[raw.React.Element] = js.native
    var anchorOrigin: js.UndefOr[Shape] = js.native
    var autoHideDuration: js.UndefOr[Int] = js.native
    var message: js.UndefOr[raw.React.Element] = js.native
    var open: js.UndefOr[Boolean] = js.native
    var resumeHideDuration: js.UndefOr[Int] = js.native
    var onClose: js.Function2[js.Dynamic, String, Unit] = js.native
    var SnackbarContentProps: js.UndefOr[js.Dynamic] = js.native
    var transition: js.UndefOr[Int] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(action: js.UndefOr[raw.React.Element] = js.undefined,
            anchorOrigin: js.UndefOr[Shape] = js.undefined,
            autoHideDurationMs: js.UndefOr[Int] = js.undefined,
            message: js.UndefOr[raw.React.Element] = js.undefined,
            open: js.UndefOr[Boolean] = js.undefined,
            resumeHideDurationMs: js.UndefOr[Int] = js.undefined,
            onClose: (js.Dynamic, String) => Callback = (_, _) => Callback.empty,
            SnackbarContentProps: js.UndefOr[js.Dynamic] = js.undefined,
            transition: js.UndefOr[Int] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.action = action
    p.anchorOrigin = anchorOrigin
    p.autoHideDuration = autoHideDurationMs
    p.message = message
    p.open = open
    p.resumeHideDuration = resumeHideDurationMs
    p.onClose = (e, msg) => onClose(e, msg).runNow()
    p.SnackbarContentProps = SnackbarContentProps
    p.transition = transition
    p.style = style

    component.withProps(p)
  }

}
