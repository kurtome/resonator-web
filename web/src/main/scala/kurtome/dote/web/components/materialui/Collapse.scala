package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-next.com/api/collapse/
  */
object Collapse {

  @JSImport("material-ui/transitions/Collapse", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object TransitionDuration {
    val Auto = TransitionDuration(true, None, None, None)

    def apply(durationMs: Int): TransitionDuration =
      TransitionDuration(false, Some(durationMs), None, None)

    def apply(enterDurationMs: Int, exitDurationMs: Int): TransitionDuration =
      TransitionDuration(false, None, Some(enterDurationMs), Some(exitDurationMs))
  }

  case class TransitionDuration(auto: Boolean,
                                durationMs: Option[Int],
                                enterDurationMs: Option[Int],
                                exitDurationMs: Option[Int])

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var in: js.UndefOr[Boolean] = js.native
    var component: js.UndefOr[String] = js.native
    var collapsedHeight: js.UndefOr[String] = js.native
    var transitionDuration: js.UndefOr[js.Any] = js.native
    var className: js.UndefOr[String] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val jsComponent = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(in: js.UndefOr[Boolean] = js.undefined,
            component: js.UndefOr[String] = js.undefined,
            collapsedHeight: js.UndefOr[String] = js.undefined,
            className: js.UndefOr[String] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.in = in
    p.component = component
    p.collapsedHeight = collapsedHeight
    p.className = className
    p.style = style

    jsComponent.withProps(p)
  }
}
