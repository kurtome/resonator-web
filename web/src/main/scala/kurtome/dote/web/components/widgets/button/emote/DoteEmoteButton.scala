package kurtome.dote.web.components.widgets.button.emote

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.Attr.Ref
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
import kurtome.dote.web.components.materialui.Grow
import kurtome.dote.web.components.materialui.Hidden
import kurtome.dote.web.components.materialui.Paper
import kurtome.dote.web.components.widgets.LongHoverTrigger
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

    val popupActionsContainer = style(
      width(100 %%),
      height(100 %%)
    )
  }

  case class Props(dotable: Dotable)
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

    val handleLikeValueChanged = (value: Boolean) =>
      Callback {
        val emote = if (value) {
          EmoteKind.HEART
        } else {
          EmoteKind.UNKNOWN_KIND
        }
        bs.modState(s => s.copy(emoteKind = emote)).runNow()
        sendDoteToServer()
    }

    def popupStyle(p: Props, s: State): StyleA = {
      Styles.popoverContent
    }

    def render(p: Props, s: State): VdomElement = {
      LongHoverTrigger(onTrigger = bs.modState(_.copy(popoverOpen = true)),
                       onFinish = bs.modState(_.copy(popoverOpen = false)))(
        Grow(in = s.popoverOpen)(
          Paper(elevation = 4, style = popupStyle(p, s))(
            <.div(
              ^.position.relative,
              GridContainer(alignItems = Grid.AlignItems.Center,
                            justify = Grid.Justify.Center,
                            style = Styles.popupActionsContainer)(
                GridItem()(
                  EmoteButton(emoji = Emojis.heart,
                              initialValue = false,
                              onValueChanged = handleLikeValueChanged)()
                ),
                GridItem()(
                  EmoteButton(emoji = Emojis.grinningSquintingFace,
                              initialValue = false,
                              onValueChanged = handleLikeValueChanged)()
                ),
                GridItem()(
                  EmoteButton(emoji = Emojis.cryingFace,
                              initialValue = false,
                              onValueChanged = handleLikeValueChanged)()
                ),
                GridItem()(
                  EmoteButton(emoji = Emojis.angryFace,
                              initialValue = false,
                              onValueChanged = handleLikeValueChanged)()
                )
              )
            )
          )
        ),
        SmileButton(
          s.emoteKind == EmoteKind.HEART,
          onValueChanged = handleLikeValueChanged
        )()
      )
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

  def apply(dotable: Dotable) =
    component.withProps(Props(dotable))
}
