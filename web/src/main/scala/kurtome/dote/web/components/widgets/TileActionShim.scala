package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.DoteRouterCtl
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.button.emote._
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.scalajs.js

object TileActionShim extends LogSupport {

  private object Styles extends StyleSheet.Inline with MuiInlineStyleSheet {
    import dsl._

    val overlayContainer = style(
      position.absolute,
      pointerEvents := "none",
      width(100 %%),
      height(100 %%)
    )

    val overlayActionsContainer = style(
      position.absolute,
      width(100 %%),
      height(100 %%)
    )

    val overlay = style(
      position.absolute,
      backgroundColor(rgba(255, 255, 255, 0.4)),
      width(100 %%),
      height(100 %%)
    )

    val clickInterceptor = style(
      position.absolute,
      pointerEvents := "auto",
      width(100 %%),
      height(100 %%)
    )

    val clickIgnore = style(
      position.absolute,
      pointerEvents := "none",
      width(100 %%),
      height(100 %%)
    )

  }
  Styles.addToDocument()
  import Styles.richStyle

  case class Props(routerCtl: DoteRouterCtl, dotable: Dotable, hover: Boolean = false)
  case class State(clicked: Boolean = false,
                   smileCount: Int = 0,
                   cryCount: Int = 0,
                   laughCount: Int = 0,
                   scowlCount: Int = 0)

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    val shouldClickToShowActions = IsMobile.value

    val sendDoteToServer: js.Function0[Unit] = Debounce.debounce0(waitMs = 2000) { () =>
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

      val showActions = if (shouldClickToShowActions) {
        s.clicked && LoggedInPersonManager.isLoggedIn
      } else {
        p.hover && LoggedInPersonManager.isLoggedIn
      }

      <.div(
        ^.className := Styles.overlayContainer,
        Fader(in = showActions, width = "100%", height = "100%")(
          <.div(
            ^.className := Styles.overlayActionsContainer,
            ^.className := SharedStyles.plainAnchor,
            <.div(^.className := Styles.overlay),
            Grid(container = true,
                 direction = Grid.Direction.Column,
                 justify = Grid.Justify.SpaceBetween,
                 spacing = 0,
                 style = Styles.overlayActionsContainer.inline)(
              Grid(item = true)(
                Grid(container = true, spacing = 0, justify = Grid.Justify.SpaceBetween)(
                  Grid(item = true)(
                    SmileButton(s.smileCount, onValueChanged = handleLikeValueChanged)()),
                  Grid(item = true)(
                    CryButton(s.cryCount, onValueChanged = handleCryValueChanged)())
                )),
              Grid(item = true)(
                Grid(container = true, spacing = 0, justify = Grid.Justify.SpaceBetween)(
                  Grid(item = true)(
                    LaughButton(s.laughCount, onValueChanged = handleLaughValueChanged)()),
                  Grid(item = true)(
                    ScowlButton(s.scowlCount, onValueChanged = handleScowlValueChanged)())
                ))
            )
          ),
          <.div(
            ^.className :=
              (if (shouldClickToShowActions && !s.clicked && LoggedInPersonManager.isLoggedIn)
                 Styles.clickInterceptor
               else Styles.clickIgnore),
            ^.onClick --> handleInterceptorClicked
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

  def apply(routerCtl: DoteRouterCtl, dotable: Dotable, hover: Boolean) =
    component.withProps(Props(routerCtl, dotable, hover))
}
