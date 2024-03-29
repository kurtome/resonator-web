package resonator.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.web.CssSettings._
import resonator.web.components.ComponentHelpers._
import resonator.web.utils._
import wvlet.log.LogSupport

object Fader extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val fadeIn = style(
      animation := s"${Animations.fadeIn.name.value} 0.5s",
      opacity(1)
    )

    val fadeOut = style(
      animation := s"${Animations.fadeOut.name.value} 0.5s",
      opacity(0)
    )

    val invisible = style(
      opacity(0)
    )
  }

  private object Animations extends StyleSheet.Inline {
    import dsl._
    val fadeIn = keyframes(
      (0 %%) -> keyframe(opacity(0)),
      (100 %%) -> keyframe(opacity(1))
    )

    val fadeOut = keyframes(
      (0 %%) -> keyframe(opacity(1)),
      (100 %%) -> keyframe(opacity(0))
    )
  }
  Animations.addToDocument()

  case class Props(in: Boolean, width: String, height: String)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    // this is a hack to make sure not to fade out when initially props.in == false
    private var wasIn = false

    def render(p: Props, pc: PropsChildren, s: State): VdomElement = {
      val style = if (p.in) {
        Styles.fadeIn
      } else if (wasIn) {
        Styles.fadeOut
      } else {
        Styles.invisible
      }
      wasIn |= p.in
      <.div(^.className := style, ^.width := p.width, ^.height := p.height, pc)
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPCS((builder, p, pc, s) => builder.backend.render(p, pc, s))
    .build

  def apply(in: Boolean, width: String = "unset", height: String = "unset")(
      c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(in, width, height))
}
