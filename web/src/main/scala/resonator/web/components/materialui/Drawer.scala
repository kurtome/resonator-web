package resonator.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/drawer/
  */
object Drawer {

  @JSImport("@material-ui/core/Drawer", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Anchors extends Enumeration {
    val Left = Value("left")
    val Right = Value("right")
    val Top = Value("top")
    val Bottom = Value("bottom")
  }
  type Anchor = Anchors.Value

  object Variants extends Enumeration {
    val Permanent = Value("permanent")
    val Persistent = Value("persistent")
    val Temporary = Value("temporary")
  }
  type Variant = Variants.Value

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var anchor: js.UndefOr[String] = js.native
    var variant: js.UndefOr[String] = js.native
    var open: js.UndefOr[Boolean] = js.native
    var onClose: js.Function0[Unit] = js.native
    var elevation: js.UndefOr[Int] = js.native
    var PaperProps: js.UndefOr[Paper.Props] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(anchor: js.UndefOr[Anchor] = js.undefined,
            variant: js.UndefOr[Variant] = js.undefined,
            open: js.UndefOr[Boolean] = js.undefined,
            onClose: Callback = Callback.empty,
            elevation: js.UndefOr[Int] = js.undefined,
            PaperProps: js.UndefOr[Paper.Props] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.anchor = anchor.map(_.toString)
    p.variant = variant.map(_.toString)
    p.open = open
    p.onClose = onClose.toJsFn
    p.elevation = elevation
    p.PaperProps = PaperProps
    p.style = style

    component.withProps(p)
  }

}
