package kurtome.dote.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.feed.FeedItemCommon
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import scalacss.internal.mutable.StyleSheet
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.materialui.MuiThemeProvider

object MainContentSection {

  object Styles extends StyleSheet.Inline {
    import dsl._

  }

  object Variants extends Enumeration {
    val Primary: Value = Value
    val Light: Value = Value
    val Default: Value = Value
  }
  type Variant = Variants.Value

  case class Props(variant: Variant, center: Boolean)

  class Backend(bs: BackendScope[Props, Unit]) extends BaseBackend(Styles) {

    private def color(p: Props): String = {
      p.variant match {
        case Variants.Light => MuiTheme.theme.palette.background.paper
        case Variants.Primary => MuiTheme.theme.palette.primary.main
        case _ => MuiTheme.theme.palette.background.default
      }
    }

    private def renderContent(p: Props, pc: PropsChildren): VdomNode = {
      if (p.center) {
        CenteredMainContent()(pc)
      } else {
        pc
      }
    }

    def render(p: Props, pc: PropsChildren): VdomElement = {
      <.div(
        ^.paddingTop := "8px",
        ^.paddingBottom := "8px",
        ^.backgroundColor := color(p),
        ^.width := "100%",
        if (p.variant == Variants.Primary) {
          // Use the dark theme so text shows up correctly on the dark background.
          MuiThemeProvider(MuiTheme.darkTheme)(
            renderContent(p, pc)
          )
        } else {
          renderContent(p, pc)
        }
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderPC((builder, props, pc) => builder.backend.render(props, pc))
    .build

  def apply(variant: Variant = Variants.Default, center: Boolean = true)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*).withProps(Props(variant, center))()

  def chooseVariant(common: FeedItemCommon): Variant = {
    common.backgroundColor match {
      case FeedItemCommon.BackgroundColor.PRIMARY => Variants.Primary
      case FeedItemCommon.BackgroundColor.LIGHT => Variants.Light
      case _ => Variants.Default
    }
  }
}
