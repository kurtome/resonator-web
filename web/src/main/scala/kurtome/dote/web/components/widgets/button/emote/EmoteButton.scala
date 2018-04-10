package kurtome.dote.web.components.widgets.button.emote

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.utils.{IsMobile, BaseBackend}
import wvlet.log.LogSupport

import scala.scalajs.js

object EmoteButton extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val wrapper = style(
      pointerEvents := auto
    )

    val inactiveIcon = style(
      color.black,
      fontSize(1.1 rem),
      opacity(0.4)
    )

    val inactiveHoverIcon = style(
      color.black,
      fontSize(1.1 rem),
      opacity(0.6)
    )

    val activeIcon = style(
      color.black,
      fontSize(1.1 rem),
      opacity(1)
    )
  }

  case class Props(emoji: String, initialValue: Boolean, onValueChanged: (Boolean) => Callback)
  case class State(value: Boolean, hover: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleClick = (p: Props, s: State) =>
      Callback {
        val newValue = !s.value
        bs.modState(s => s.copy(value = newValue)).runNow()
        p.onValueChanged(newValue).runNow()
    }

    def pickStyle(s: State): js.Dynamic = {
      if (s.value) {
        Styles.activeIcon
      } else {
        if (s.hover) {
          Styles.inactiveHoverIcon
        } else {
          Styles.inactiveIcon
        }
      }
    }

    def render(p: Props, s: State): VdomElement = {
      <.div(
        ^.className := Styles.wrapper,
        ^.onMouseEnter --> bs.modState(_.copy(hover = true)),
        ^.onMouseLeave --> bs.modState(_.copy(hover = false)),
        IconButton(onClick = handleClick(p, s), style = pickStyle(s))(
          p.emoji
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => State(value = p.initialValue))
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(emoji: String, initialValue: Boolean, onValueChanged: (Boolean) => Callback) =
    component.withProps(Props(emoji, initialValue, onValueChanged))

}
