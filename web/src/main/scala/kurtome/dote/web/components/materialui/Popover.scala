package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.VdomNode
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-next.com/api/popovers/
  */
object Popover {

  @JSImport("material-ui/Popover", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object HorizontalOrigins extends Enumeration {
    val Left = Value("left")
    val Center = Value("center")
    val Right = Value("right")
  }
  type HorizontalOrigin = HorizontalOrigins.Value

  object VerticalOrigins extends Enumeration {
    val Top = Value("top")
    val Center = Value("center")
    val Bottom = Value("bottom")
  }
  type VerticalOrigin = VerticalOrigins.Value

  @js.native
  trait Origin extends js.Object {
    var vertical: String = js.native
    var horizontal: String = js.native
  }
  object Origin {
    def apply(vertical: VerticalOrigin, horizontal: HorizontalOrigin): Origin = {
      val origin = new js.Object().asInstanceOf[Origin]
      origin.vertical = vertical.toString
      origin.horizontal = horizontal.toString
      origin
    }
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var style: js.UndefOr[js.Dynamic] = js.native
    var open: js.UndefOr[Boolean] = js.native
    var anchorEl: js.UndefOr[dom.Element] = js.native
    var PaperProps: js.UndefOr[Paper.Props] = js.native
    var anchorOrigin: js.UndefOr[Origin] = js.native
    var transformOrigin: js.UndefOr[Origin] = js.native
    var onClose: js.Function0[Unit] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(style: js.UndefOr[js.Dynamic] = js.undefined,
            open: js.UndefOr[Boolean] = js.undefined,
            anchorEl: js.UndefOr[dom.Element] = js.undefined,
            PaperProps: js.UndefOr[Paper.Props] = js.undefined,
            anchorOrigin: js.UndefOr[Origin] = js.undefined,
            transformOrigin: js.UndefOr[Origin] = js.undefined,
            onClose: Callback = Callback.empty) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.style = style
    p.open = open
    p.PaperProps = PaperProps
    p.anchorEl = anchorEl
    p.anchorOrigin = anchorOrigin
    p.transformOrigin = transformOrigin
    p.onClose = onClose.toJsFn
    component.withProps(p)
  }
}
