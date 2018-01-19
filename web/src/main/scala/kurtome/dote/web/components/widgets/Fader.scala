package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

object Fader extends LogSupport {

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

  private object Styles extends StyleSheet.Inline with MuiInlineStyleSheet {
    import dsl._

    val fadeIn = style(
      animation := s"${Animations.fadeIn.name.value} 0.5s",
      opacity(1)
    )

    val fadeOut = style(
      animation := s"${Animations.fadeOut.name.value} 0.5s",
      opacity(0)
    )
  }
  Styles.addToDocument()

  case class Props(in: Boolean, width: String, height: String)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    def render(p: Props, pc: PropsChildren, s: State): VdomElement = {
      debug(p)
      <.div(^.className := (if (p.in) Styles.fadeIn else Styles.fadeOut),
            ^.width := p.width,
            ^.height := p.height,
            pc)
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
