package kurtome.dote.web.components.widgets.button.emote

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.Attr.Ref
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.proto.api.dote.Dote.EmoteKind
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.materialui.Paper
import kurtome.dote.web.components.materialui.Popover
import kurtome.dote.web.components.materialui.Popover.HorizontalOrigins
import kurtome.dote.web.components.materialui.Popover.VerticalOrigins
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
      padding(4 px)
    )
  }

  case class Props(dotable: Dotable)
  case class State(smileCount: Int = 0,
                   emoteKind: EmoteKind = EmoteKind.UNKNOWN_KIND,
                   popoverOpen: Boolean = false,
                   buttonDomNode: dom.Element = null)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val sendDoteToServer: js.Function0[Unit] = Debounce.debounce0(waitMs = 200) { () =>
      val p: Props = bs.props.runNow()
      val s: State = bs.state.runNow()

      val f =
        DoteProtoServer.setDote(
          SetDoteRequest(p.dotable.id,
                         Some(Dote(smileCount = s.smileCount, emoteKind = s.emoteKind))))
      GlobalLoadingManager.addLoadingFuture(f)
    }

    val handleLikeValueChanged = (value: Int) =>
      Callback {
        val emote = if (value > 0) {
          EmoteKind.HEART
        } else {
          EmoteKind.UNKNOWN_KIND
        }
        bs.modState(s => s.copy(smileCount = value, emoteKind = emote)).runNow()
        sendDoteToServer()
    }

    val buttonRef = ScalaComponent.mutableRefTo(SmileButton.component)

    val handleDidMount: Callback = bs.modState(_.copy(buttonDomNode = buttonRef.value.getDOMNode))

    def render(p: Props, s: State): VdomElement = {

      LongHoverTrigger(onTrigger = bs.modState(_.copy(popoverOpen = true)))(
        Popover(
          open = s.popoverOpen,
          onClose = bs.modState(_.copy(popoverOpen = false)),
          anchorEl = s.buttonDomNode,
          PaperProps = Paper.Props(elevation = 4, style = Styles.popoverContent),
          anchorOrigin = Popover.Origin(VerticalOrigins.Top, HorizontalOrigins.Center),
          transformOrigin = Popover.Origin(VerticalOrigins.Bottom, HorizontalOrigins.Center)
        )("Like"),
        buttonRef.component(
          SmileButton.Props(
            if (s.emoteKind == EmoteKind.HEART) 1 else 0,
            onValueChanged = handleLikeValueChanged
          ))
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => {
      val dote = p.dotable.getDote
      State(smileCount = dote.smileCount, emoteKind = dote.emoteKind)
    })
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .componentDidMount(x => x.backend.handleDidMount)
    .build

  def apply(dotable: Dotable) =
    component.withProps(Props(dotable))
}
