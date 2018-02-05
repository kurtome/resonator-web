package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._
import kurtome.dote.web.components.ComponentHelpers.DangerousInnerHtml

import scala.scalajs.js
import scala.scalajs.js.annotation._

/**
  * Wrapper for https://material-ui-next.com/api/typography/
  */
object Typography {

  @JSImport("material-ui/Typography/Typography.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  object Aligns extends Enumeration {
    val Inherit = Value("inherit")
    val Center = Value("center")
    val Left = Value("left")
    val Right = Value("right")
    val Justify = Value("justify")
  }
  type Align = Aligns.Value

  object Colors extends Enumeration {
    val Inherit = Value("inherit")
    val Default = Value("default")
    val Primary = Value("primary")
    val Secondary = Value("secondary")
    val Error = Value("error")
  }
  type Color = Colors.Value

  object Variants extends Enumeration {
    val Display1 = Value("display1")
    val Display2 = Value("display2")
    val Display3 = Value("display3")
    val Display4 = Value("display4")
    val Headline = Value("headline")
    val Title = Value("title")
    val SubHeading = Value("subheading")
    val Body1 = Value("body1")
    val Body2 = Value("body2")
    val Caption = Value("caption")
    val Button = Value("button")
  }
  type Variant = Variants.Value

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var align: js.UndefOr[String] = js.native
    var color: js.UndefOr[String] = js.native
    var gutterBottom: js.UndefOr[Boolean] = js.native
    var noWrap: js.UndefOr[Boolean] = js.native
    var paragraph: js.UndefOr[Boolean] = js.native
    var variant: js.UndefOr[String] = js.native
    var className: js.UndefOr[String] = js.native
    var component: js.UndefOr[String] = js.native
    var style: js.UndefOr[js.Dynamic] = js.native
    var dangerouslySetInnerHTML: js.UndefOr[DangerousInnerHtml] = js.native
  }

  val reactComponent = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(align: js.UndefOr[Align] = js.undefined,
            color: js.UndefOr[Color] = js.undefined,
            gutterBottom: js.UndefOr[Boolean] = js.undefined,
            noWrap: js.UndefOr[Boolean] = js.undefined,
            paragraph: js.UndefOr[Boolean] = js.undefined,
            className: js.UndefOr[String] = js.undefined,
            component: js.UndefOr[String] = js.undefined,
            style: js.UndefOr[js.Dynamic] = js.undefined,
            variant: js.UndefOr[Variant] = js.undefined,
            dangerouslySetInnerHTML: js.UndefOr[DangerousInnerHtml] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.align = align.map(_.toString)
    p.color = color.map(_.toString)
    p.variant = variant.map(_.toString)
    p.gutterBottom = gutterBottom
    p.noWrap = noWrap
    p.paragraph = paragraph
    p.component = component
    p.className = className
    p.style = style
    p.dangerouslySetInnerHTML = dangerouslySetInnerHTML

    reactComponent.withProps(p)
  }

}
