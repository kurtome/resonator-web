package resonator.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.feed.FeedItemCommon
import resonator.web.constants.MuiTheme
import resonator.web.utils.BaseBackend
import resonator.web.components.ComponentHelpers._
import scalacss.internal.mutable.StyleSheet
import resonator.web.CssSettings._
import resonator.web.components.materialui.MuiThemeProvider

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

  case class Props(variant: Variant, center: Boolean, verticalPaddingPx: Int)

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
        ^.paddingTop := asPxStr(p.verticalPaddingPx),
        ^.paddingBottom := asPxStr(p.verticalPaddingPx),
        ^.backgroundColor := color(p),
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

  def apply(variant: Variant = Variants.Default,
            center: Boolean = true,
            verticalPaddingPx: Int = 16)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*).withProps(Props(variant, center, verticalPaddingPx))()

  def chooseVariant(common: FeedItemCommon): Variant = {
    common.backgroundColor match {
      case FeedItemCommon.BackgroundColor.PRIMARY => Variants.Primary
      case FeedItemCommon.BackgroundColor.LIGHT => Variants.Light
      case _ => Variants.Default
    }
  }
}
