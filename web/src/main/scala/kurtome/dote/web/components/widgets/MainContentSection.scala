package kurtome.dote.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import scalacss.internal.mutable.StyleSheet
import kurtome.dote.web.CssSettings._

object MainContentSection {

  object Styles extends StyleSheet.Inline {
    import dsl._

  }

  object Variants extends Enumeration {
    val Primary: Value = Value
    val White: Value = Value
    val Default: Value = Value
  }
  type Variant = Variants.Value

  case class Props(variant: Variant)

  class Backend(bs: BackendScope[Props, Unit]) extends BaseBackend(Styles) {

    def color(p: Props): String = {
      p.variant match {
        case Variants.White => MuiTheme.theme.palette.common.white
        case Variants.Primary => MuiTheme.theme.palette.primary.main
        case _ => MuiTheme.theme.palette.background.default
      }
    }

    def render(p: Props, pc: PropsChildren): VdomElement = {
      <.div(
        ^.paddingTop := "16px",
        ^.paddingBottom := "16px",
        ^.backgroundColor := color(p),
        ^.width := "100%",
        CenteredMainContent()(pc)
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderPC((builder, props, pc) => builder.backend.render(props, pc))
    .build

  def apply(variant: Variant = Variants.Default)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*).withProps(Props(variant))()
}
