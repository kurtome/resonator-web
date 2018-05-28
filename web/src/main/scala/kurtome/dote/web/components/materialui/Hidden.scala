package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/hidden/
  */
object Hidden {

  @JSImport("@material-ui/core/Hidden", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  case class TransitionDuration(auto: Boolean,
                                durationMs: Option[Int],
                                enterDurationMs: Option[Int],
                                exitDurationMs: Option[Int])

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var xsUp: js.UndefOr[Boolean] = js.native
    var xsDown: js.UndefOr[Boolean] = js.native
    var smUp: js.UndefOr[Boolean] = js.native
    var smDown: js.UndefOr[Boolean] = js.native
    var mdUp: js.UndefOr[Boolean] = js.native
    var mdDown: js.UndefOr[Boolean] = js.native
  }
  object Props {
    def apply(xsUp: js.UndefOr[Boolean] = js.undefined,
              xsDown: js.UndefOr[Boolean] = js.undefined,
              smDown: js.UndefOr[Boolean] = js.undefined,
              smUp: js.UndefOr[Boolean] = js.undefined,
              mdDown: js.UndefOr[Boolean] = js.undefined,
              mdUp: js.UndefOr[Boolean] = js.undefined): Props = {
      val p = (new js.Object).asInstanceOf[Props]
      p.xsUp = xsUp
      p.xsDown = xsDown
      p.smUp = smUp
      p.smDown = smDown
      p.mdUp = mdUp
      p.mdDown = mdDown
      p
    }
  }

  val jsComponent = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(xsUp: js.UndefOr[Boolean] = js.undefined,
            xsDown: js.UndefOr[Boolean] = js.undefined,
            smDown: js.UndefOr[Boolean] = js.undefined,
            smUp: js.UndefOr[Boolean] = js.undefined,
            mdDown: js.UndefOr[Boolean] = js.undefined,
            mdUp: js.UndefOr[Boolean] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.xsUp = xsUp
    p.xsDown = xsDown
    p.smUp = smUp
    p.smDown = smDown
    p.mdUp = mdUp
    p.mdDown = mdDown

    jsComponent.withProps(p)
  }
}
