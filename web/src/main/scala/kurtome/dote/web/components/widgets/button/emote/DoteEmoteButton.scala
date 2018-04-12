package kurtome.dote.web.components.widgets.button.emote

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.proto.api.dote.Dote.EmoteKind
import kurtome.dote.shared.constants.Emojis
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui.GridContainer
import kurtome.dote.web.components.materialui.GridItem
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils._
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js

object DoteEmoteButton extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val popoverContent = style(
      zIndex(10),
      position.absolute,
      marginTop(-48 px),
      marginLeft(-48 * 1.5 px),
      height(48 px),
      width(48 * 4 px),
      padding(4 px)
    )
  }

  case class Props(dotable: Dotable, showAllOptions: Boolean)
  case class State(emoteKind: EmoteKind = EmoteKind.UNKNOWN_KIND,
                   popoverOpen: Boolean = false,
                   buttonDomNode: dom.Element = null)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val sendDoteToServer: js.Function0[Unit] = Debounce.debounce0(waitMs = 200) { () =>
      val p: Props = bs.props.runNow()
      val s: State = bs.state.runNow()

      val f =
        DoteProtoServer.setDote(SetDoteRequest(p.dotable.id, Some(Dote(emoteKind = s.emoteKind))))
      GlobalLoadingManager.addLoadingFuture(f)
    }

    def handleEmoteValueChanged(s: State, emoteKind: EmoteKind, shouldToggleOff: Boolean = false) =
      Callback {
        val isActive = s.emoteKind != EmoteKind.UNKNOWN_KIND
        val emote =
          if (shouldToggleOff && isActive) {
            EmoteKind.UNKNOWN_KIND
          } else if (!shouldToggleOff && s.emoteKind == emoteKind) {
            EmoteKind.UNKNOWN_KIND
          } else {
            emoteKind
          }
        bs.modState(s => s.copy(popoverOpen = false, emoteKind = emote)).runNow()
        sendDoteToServer()
      }

    def popupStyle(p: Props, s: State): StyleA = {
      Styles.popoverContent
    }

    def render(p: Props, s: State): VdomElement = {
      if (p.showAllOptions) {
        GridContainer(alignItems = Grid.AlignItems.Center, justify = Grid.Justify.Center)(
          GridItem()(
            EmoteButton(emoji = Emojis.heart,
                        active = s.emoteKind == EmoteKind.HEART,
                        onToggle = handleEmoteValueChanged(s, EmoteKind.HEART))()
          ),
          GridItem()(
            EmoteButton(emoji = Emojis.cryingFace,
                        active = s.emoteKind == EmoteKind.CRY,
                        onToggle = handleEmoteValueChanged(s, EmoteKind.CRY))()
          ),
          GridItem()(
            EmoteButton(emoji = Emojis.grinningSquintingFace,
                        active = s.emoteKind == EmoteKind.LAUGH,
                        onToggle = handleEmoteValueChanged(s, EmoteKind.LAUGH))()
          ),
          GridItem()(
            EmoteButton(emoji = Emojis.angryFace,
                        active = s.emoteKind == EmoteKind.SCOWL,
                        onToggle = handleEmoteValueChanged(s, EmoteKind.SCOWL))()
          )
        )
      } else {
        EmoteButton(
          emoji = Emojis.pickEmoji(s.emoteKind, default = Emojis.heart),
          active = s.emoteKind != EmoteKind.UNKNOWN_KIND,
          onToggle = handleEmoteValueChanged(s, EmoteKind.HEART, shouldToggleOff = true)
        )()
      }
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => {
      val dote = p.dotable.getDote
      State(emoteKind = dote.emoteKind)
    })
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable, showAllOptions: Boolean = false) =
    component.withProps(Props(dotable, showAllOptions))
}
