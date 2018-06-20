package resonator.web.components.widgets

import japgolly.scalajs.react
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.dotable.Dotable
import resonator.web.CssSettings._
import resonator.web.DoteRoutes._
import resonator.web.components.ComponentHelpers._
import resonator.web.components.materialui.Paper
import resonator.web.constants.MuiTheme
import resonator.web.utils._
import wvlet.log.LogSupport

object HoverPaper extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val cardHeader = style(
      backgroundColor :=! MuiTheme.theme.palette.extras.cardHeader
    )

    val default = style(
      backgroundColor :=! MuiTheme.theme.palette.background.paper
    )

    val accent = style(
      backgroundColor :=! MuiTheme.theme.palette.extras.accentPaperBackground
    )

  }

  object Variants extends Enumeration {
    val Accent = Value // recent activity feed
    val CardHeader = Value // recent activity feed
    val Default = Value
  }
  type Variant = Variants.Value

  case class Props(elevation: Int, variant: Variant)
  case class State(hover: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def paperStyle(p: Props): StyleA = {
      p.variant match {
        case Variants.Accent => Styles.accent
        case Variants.CardHeader => Styles.cardHeader
        case _ => Styles.default
      }
    }

    def render(p: Props, s: State, pc: PropsChildren): VdomElement = {
      <.div(
        ^.onMouseEnter --> bs.modState(_.copy(hover = true)),
        ^.onMouseLeave --> bs.modState(_.copy(hover = false)),
        Paper(style = paperStyle(p), elevation = if (s.hover) p.elevation + 2 else p.elevation)(pc)
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPCS((builder, p, pc, s) => builder.backend.render(p, s, pc))
    .build

  def apply(elevation: Int = 0, variant: Variant = Variants.Default) =
    component.withProps(Props(elevation, variant))
}
