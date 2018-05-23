package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui.com/api/collapse/
  */
object Collapse {

  @JSImport("@material-ui/core/Collapse", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var in: js.UndefOr[Boolean] = js.native
    var component: js.UndefOr[String] = js.native
    var collapsedHeight: js.UndefOr[String] = js.native
    var className: js.UndefOr[String] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  val jsComponent = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(in: js.UndefOr[Boolean] = js.undefined,
            component: js.UndefOr[String] = "div",
            collapsedHeight: js.UndefOr[String] = js.undefined,
            className: js.UndefOr[String] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    if (in.isDefined) {
      p.in = in
    }
    if (component.isDefined) {
      p.component = component
    }
    if (collapsedHeight.isDefined) {
      p.collapsedHeight = collapsedHeight
    }
    if (className.isDefined) {
      p.className = className
    }
    if (style.isDefined) {
      p.style = style
    }

    jsComponent.withProps(p)
  }
}
