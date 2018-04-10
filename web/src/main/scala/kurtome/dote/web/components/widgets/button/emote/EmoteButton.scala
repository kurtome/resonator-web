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

  case class Props(emojis: Seq[String], initialValue: Int, onValueChanged: (Int) => Callback)
  case class State(valueCount: Int = 0, hover: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleClick = (p: Props, s: State) =>
      Callback {
        val maxValue = p.emojis.size + 1
        val newValue = (s.valueCount + 1) % maxValue
        bs.modState(s => s.copy(valueCount = newValue))
          .runNow()
        p.onValueChanged(newValue).runNow()
    }

    def pickEmoji(p: Props, s: State): String = {
      val index = Math.min(Math.max(0, s.valueCount - 1), p.emojis.size - 1)
      p.emojis(index)
    }

    def pickStyle(s: State): js.Dynamic = {
      if (s.valueCount == 0) {
        if (s.hover) {
          Styles.inactiveHoverIcon
        } else {
          Styles.inactiveIcon
        }
      } else {
        Styles.activeIcon
      }
    }

    def render(p: Props, s: State): VdomElement = {
      <.div(
        ^.className := Styles.wrapper,
        ^.onMouseEnter --> bs.modState(_.copy(hover = true)),
        ^.onMouseLeave --> bs.modState(_.copy(hover = false)),
        IconButton(onClick = handleClick(p, s), style = pickStyle(s))(
          pickEmoji(p, s)
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => State(valueCount = p.initialValue))
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(emojis: Seq[String], initialValue: Int, onValueChanged: (Int) => Callback) =
    component.withProps(Props(emojis, initialValue, onValueChanged))

}
