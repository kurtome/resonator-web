package kurtome.dote.web.components.lib

import japgolly.scalajs.react
import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for https://github.com/rexxars/react-markdown
  */
object Markdown {

  @JSImport("react-markdown", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var source: String = js.native
    var className: js.UndefOr[String] = js.native
    var escapeHtml: js.UndefOr[Boolean] = js.native
    var unwrapDisallowed: js.UndefOr[Boolean] = js.native
    var allowedTypes: js.UndefOr[js.Array[String]] = js.native
    var disallowedTypes: js.UndefOr[js.Array[String]] = js.native
  }

  private val jsComponent = JsComponent[Props, Children.None, Null](RawComponent)

  def apply(source: String, component: js.UndefOr[String] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.source = source
    p.className = component

    jsComponent.withProps(p)
  }
}
