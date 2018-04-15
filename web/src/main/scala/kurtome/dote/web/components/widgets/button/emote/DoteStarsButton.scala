package kurtome.dote.web.components.widgets.button.emote

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui.GridContainer
import kurtome.dote.web.components.materialui.GridItem
import kurtome.dote.web.components.materialui.IconButton
import kurtome.dote.web.components.materialui.Icons
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.scalajs.js

object DoteStarsButton extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val starButton = style(
      width(24 px)
    )

  }

  case class Props(dotable: Dotable, onDoteChanged: (Dote) => Callback)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val sendDoteToServer: js.Function1[Int, Unit] = Debounce.debounce1(waitMs = 200) {
      (numHalfStars: Int) =>
        val p: Props = bs.props.runNow()

        val dote = p.dotable.getDote.withHalfStars(numHalfStars)

        p.onDoteChanged(dote).runNow()

        val f =
          DoteProtoServer.setDote(SetDoteRequest(p.dotable.id, Some(dote)))
        GlobalLoadingManager.addLoadingFuture(f)
    }

    def handleStarValueChanged(p: Props, starPosition: Int) =
      Callback {
        val newNumHalfStars = if (p.dotable.getDote.halfStars < ((starPosition * 2) - 1)) {
          starPosition * 2
        } else if (p.dotable.getDote.halfStars == ((starPosition * 2) - 1)) {
          0
        } else {
          (starPosition * 2) - 1
        }
        sendDoteToServer(newNumHalfStars)
      }

    private def renderStarButton(p: Props, starPosition: Int) = {
      val icon = if (p.dotable.getDote.halfStars < ((starPosition * 2) - 1)) {
        Icons.StarOutline()
      } else if (p.dotable.getDote.halfStars == ((starPosition * 2) - 1)) {
        Icons.StarHalf()
      } else {
        Icons.Star()
      }
      IconButton(style = Styles.starButton, onClick = handleStarValueChanged(p, starPosition))(
        icon)
    }

    def render(p: Props, s: State): VdomElement = {
      val kind = p.dotable.getDote.emoteKind
      GridContainer(alignItems = Grid.AlignItems.Center, wrap = Grid.Wrap.NoWrap)(
        GridItem()(
          renderStarButton(p, 1)
        ),
        GridItem()(
          renderStarButton(p, 2)
        ),
        GridItem()(
          renderStarButton(p, 3)
        ),
        GridItem()(
          renderStarButton(p, 4)
        ),
        GridItem()(
          renderStarButton(p, 5)
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable, onDoteChanged: (Dote) => Callback = _ => Callback.empty) =
    component.withProps(Props(dotable, onDoteChanged))
}