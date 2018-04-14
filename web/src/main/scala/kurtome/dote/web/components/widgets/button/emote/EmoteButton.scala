package kurtome.dote.web.components.widgets.button.emote

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.utils._
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
      width(30 px),
      fontSize(1.1 rem),
      opacity(0.4)
    )

    val inactiveHoverIcon = style(
      color.black,
      width(30 px),
      fontSize(1.1 rem),
      opacity(0.6)
    )

    val activeIcon = style(
      color.black,
      width(30 px),
      fontSize(1.1 rem),
      opacity(1)
    )
  }

  case class Props(emoji: String, active: Boolean, onToggle: Callback)
  case class State(hover: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def pickStyle(p: Props, s: State): js.Dynamic = {
      if (p.active) {
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
        IconButton(onClick = p.onToggle, style = pickStyle(p, s))(
          p.emoji
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => State(p.active))
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(emoji: String, active: Boolean = false, onToggle: Callback) =
    component.withProps(Props(emoji, active, onToggle))

}
