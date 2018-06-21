package resonator.web.components.widgets.button.emote

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.action.set_dote.SetDoteRequest
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.dote.Dote
import resonator.proto.api.dote.Dote.EmoteKind
import resonator.shared.constants.Emojis
import resonator.web.CssSettings._
import resonator.web.components.materialui.Grid
import resonator.web.components.materialui.GridContainer
import resonator.web.components.materialui.GridItem
import resonator.web.rpc.ResonatorApiClient
import resonator.web.utils._
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js

object DoteEmoteButton extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

  }

  case class Props(dotable: Dotable,
                   showAllOptions: Boolean,
                   onDoteChanged: (Dote) => Callback,
                   sendToServer: Boolean)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val updateDote: js.Function1[EmoteKind, Unit] = Debounce.debounce1(waitMs = 200) {
      (emoteKind: EmoteKind) =>
        val p: Props = bs.props.runNow()
        val s: State = bs.state.runNow()

        val dote = p.dotable.getDote.withEmoteKind(emoteKind)

        p.onDoteChanged(dote).runNow()

        if (p.sendToServer) {
          val f =
            ResonatorApiClient.setDote(SetDoteRequest(p.dotable.id, Some(dote)))
          GlobalLoadingManager.addLoadingFuture(f)
        }
    }

    def handleEmoteValueChanged(p: Props,
                                buttonKind: EmoteKind,
                                shouldToggleOff: Boolean = false) =
      Callback {
        val isActive = p.dotable.getDote.emoteKind != EmoteKind.UNKNOWN_KIND
        val newEmoteKind =
          if (shouldToggleOff && isActive) {
            EmoteKind.UNKNOWN_KIND
          } else if (!shouldToggleOff && p.dotable.getDote.emoteKind == buttonKind) {
            EmoteKind.UNKNOWN_KIND
          } else {
            buttonKind
          }
        updateDote(newEmoteKind)
      }

    def render(p: Props, s: State): VdomElement = {
      val kind = p.dotable.getDote.emoteKind
      if (p.showAllOptions) {
        GridContainer(alignItems = Grid.AlignItems.Center)(
          GridItem()(
            EmoteButton(emoji = Emojis.heart,
                        active = kind == EmoteKind.HEART,
                        onToggle = handleEmoteValueChanged(p, EmoteKind.HEART))()
          ),
          GridItem()(
            EmoteButton(emoji = Emojis.cryingFace,
                        active = kind == EmoteKind.CRY,
                        onToggle = handleEmoteValueChanged(p, EmoteKind.CRY))()
          ),
          GridItem()(
            EmoteButton(emoji = Emojis.grinningSquintingFace,
                        active = kind == EmoteKind.LAUGH,
                        onToggle = handleEmoteValueChanged(p, EmoteKind.LAUGH))()
          ),
          GridItem()(
            EmoteButton(emoji = Emojis.angryFace,
                        active = kind == EmoteKind.SCOWL,
                        onToggle = handleEmoteValueChanged(p, EmoteKind.SCOWL))()
          ),
          GridItem()(
            EmoteButton(emoji = Emojis.thinkingFace,
                        active = kind == EmoteKind.THINK,
                        onToggle = handleEmoteValueChanged(p, EmoteKind.THINK))()
          )
        )
      } else {
        EmoteButton(
          emoji = Emojis.pickEmoji(kind, default = Emojis.heart),
          active = kind != EmoteKind.UNKNOWN_KIND,
          onToggle = handleEmoteValueChanged(p, EmoteKind.HEART, shouldToggleOff = true)
        )()
      }
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable,
            showAllOptions: Boolean = false,
            onDoteChanged: (Dote) => Callback = _ => Callback.empty,
            sendToServer: Boolean = true) =
    component.withProps(Props(dotable, showAllOptions, onDoteChanged, sendToServer))
}
