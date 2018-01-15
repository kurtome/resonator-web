package kurtome.dote.web.components.widgets.button.emote

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.constants.StringValues

object SmileButton {

  case class Props(onValueChanged: (Int) => Callback)
  case class State(valueCount: Int = 0)

  class Backend(bs: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      import StringValues.Emojis._
      EmoteButton(emojis = Seq(slightlySmilingFace, grinningFace, heartEyes),
                  onValueChanged = p.onValueChanged)()
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(onValueChanged: (Int) => Callback) =
    component.withProps(Props(onValueChanged))
}
