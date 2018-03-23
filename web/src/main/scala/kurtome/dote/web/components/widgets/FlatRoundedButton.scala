package kurtome.dote.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.materialui.Button
import kurtome.dote.web.components.materialui.Typography
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import scalacss.internal.mutable.StyleSheet

object FlatRoundedButton {

  object Styles extends StyleSheet.Inline {
    import dsl._

    private val roundCornerRadius = 40 px

    val fillPrimaryLightButton = style(
      backgroundColor :=! MuiTheme.theme.palette.primary.light,
      borderRadius(roundCornerRadius)
    )

    val fillWhiteButton = style(
      backgroundColor :=! MuiTheme.theme.palette.common.white,
      borderRadius(roundCornerRadius)
    )

    val borderPrimaryLightButton = style(
      borderColor :=! MuiTheme.theme.palette.primary.light,
      borderWidth(2 px),
      borderRadius(roundCornerRadius)
    )

    val whiteText = style(
      color :=! MuiTheme.theme.palette.common.white
    )

    val lightPrimaryText = style(
      color :=! MuiTheme.theme.palette.primary.light
    )
  }

  case class Props(variant: Variant, onClick: Callback)

  object Variants extends Enumeration {
    val Fill: Value = Value
    val FillWhite: Value = Value
    val Border: Value = Value
  }
  type Variant = Variants.Value

  class Backend(bs: BackendScope[Props, Unit]) extends BaseBackend(Styles) {

    def textStyle(p: Props): StyleA = {
      // text color should be opposite of the button color
      p.variant match {
        case Variants.Fill => Styles.whiteText
        case _ => Styles.lightPrimaryText
      }
    }

    def buttonStyle(p: Props): StyleA = {
      p.variant match {
        case Variants.Fill => Styles.fillPrimaryLightButton
        case Variants.FillWhite => Styles.fillWhiteButton
        case _ => Styles.borderPrimaryLightButton
      }
    }

    def render(p: Props, pc: PropsChildren): VdomElement = {
      Button(style = buttonStyle(p), onClick = p.onClick)(
        Typography(variant = Typography.Variants.SubHeading, style = textStyle(p))(pc))
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderPC((builder, props, pc) => builder.backend.render(props, pc))
    .build

  def apply(variant: Variant = Variants.Border, onClick: Callback = Callback.empty)(
      c: CtorType.ChildArg*) =
    component.withChildren(c: _*).withProps(Props(variant, onClick))()
}
