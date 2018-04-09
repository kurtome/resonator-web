package kurtome.dote.web.components.widgets.card

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.Fader
import kurtome.dote.web.components.widgets.button.ShareButton
import kurtome.dote.web.components.widgets.button.emote._
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils._
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js

object CardActionShim extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val overlayContainer = style(
      zIndex(100),
      position.absolute,
      pointerEvents := "none",
      width(100 %%),
      height(100 %%)
    )

    val actionsContainerWrapper = style(
      marginLeft(4 px),
      marginRight(4 px)
    )

    val actionsContainer = style(
      backgroundColor(rgba(255, 255, 255, 0.8)),
      borderRadius(5 px)
    )

    val overlay = style(
      position.absolute,
      width(100 %%),
      height(100 %%)
    )

    val buttonItem = style(
      pointerEvents := "auto"
    )

  }

  case class Props(dotable: Dotable, hover: Boolean = false)
  case class State(clicked: Boolean = false,
                   smileCount: Int = 0,
                   cryCount: Int = 0,
                   laughCount: Int = 0,
                   scowlCount: Int = 0)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val sendDoteToServer: js.Function0[Unit] = Debounce.debounce0(waitMs = 200) { () =>
      val p: Props = bs.props.runNow()
      val s: State = bs.state.runNow()

      debug(s"sending $s")

      val f = DoteProtoServer.setDote(
        SetDoteRequest(p.dotable.id,
                       Some(
                         Dote(smileCount = s.smileCount,
                              laughCount = s.laughCount,
                              cryCount = s.cryCount,
                              scowlCount = s.scowlCount))))
      GlobalLoadingManager.addLoadingFuture(f)
    }

    val handleLikeValueChanged = (value: Int) =>
      Callback {
        bs.modState(s => s.copy(smileCount = value)).runNow()
        sendDoteToServer()
    }

    val handleCryValueChanged = (value: Int) =>
      Callback {
        bs.modState(s => s.copy(cryCount = value)).runNow()
        sendDoteToServer()
    }

    val handleLaughValueChanged = (value: Int) =>
      Callback {
        bs.modState(s => s.copy(laughCount = value)).runNow()
        sendDoteToServer()
    }

    val handleScowlValueChanged = (value: Int) =>
      Callback {
        bs.modState(s => s.copy(scowlCount = value)).runNow()
        sendDoteToServer()
    }

    val handleInterceptorClicked = bs.modState(_.copy(clicked = true))

    def render(p: Props, s: State): VdomElement = {
      val showActions = p.hover && LoggedInPersonManager.isLoggedIn

      <.div(
        ^.className := Styles.overlayContainer,
        Fader(in = showActions)(
          GridContainer(justify = Grid.Justify.FlexEnd,
                        alignItems = Grid.AlignItems.Center,
                        spacing = 0,
                        style = Styles.overlay)(
            GridItem(style = Styles.actionsContainerWrapper)(
              GridContainer(style = Styles.actionsContainer,
                            direction = Grid.Direction.Column,
                            spacing = 0,
                            justify = Grid.Justify.SpaceBetween)(
                GridItem(style = Styles.buttonItem)(ShareButton(
                  s"${dom.document.domain}/details/${p.dotable.id}/${p.dotable.slug}")()),
                GridItem(style = Styles.buttonItem)(
                  DoteEmoteButton(p.dotable)()
                )
              )
            )
          )
        )
      )

    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => {
      val dote = p.dotable.getDote
      State(smileCount = dote.smileCount,
            cryCount = dote.cryCount,
            laughCount = dote.laughCount,
            scowlCount = dote.scowlCount)
    })
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable, hover: Boolean) =
    component.withProps(Props(dotable, hover))
}
