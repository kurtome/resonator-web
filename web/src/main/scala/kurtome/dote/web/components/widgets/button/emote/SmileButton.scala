package kurtome.dote.web.components.widgets.button.emote

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.constants.Emojis

object SmileButton {

  case class Props(initialValue: Int, onValueChanged: (Int) => Callback)

  class Backend(bs: BackendScope[Props, Unit]) {
    def render(p: Props): VdomElement = {
      import Emojis._
      EmoteButton(emojis = Seq(heart),
                  initialValue = p.initialValue,
                  onValueChanged = p.onValueChanged)()
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderP((builder, p) => builder.backend.render(p))
    .build

  def apply(initialValue: Int, onValueChanged: (Int) => Callback) =
    component.withProps(Props(initialValue, onValueChanged))
}
