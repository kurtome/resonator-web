package resonator.web.components.widgets.card

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.action.set_dote.SetDoteRequest
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.dote.Dote
import resonator.web.CssSettings._
import resonator.web.SharedStyles
import resonator.web.components.materialui._
import resonator.web.components.ComponentHelpers._
import resonator.web.components.widgets.Fader
import resonator.web.components.widgets.button.ShareButton
import resonator.web.components.widgets.button.emote._
import resonator.web.rpc.ResonatorApiClient
import resonator.web.utils._
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

  case class Props(dotable: Dotable, hover: Boolean = false, onDoteChanged: (Dote) => Callback)
  case class State(clicked: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {
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
                GridItem(style = Styles.buttonItem)(ShareButton(dotableUrl(p.dotable))()),
                GridItem(style = Styles.buttonItem)(
                  DoteEmoteButton(p.dotable, onDoteChanged = p.onDoteChanged)()
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
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable,
            active: Boolean,
            onDoteChanged: (Dote) => Callback = _ => Callback.empty) =
    component.withProps(Props(dotable, active, onDoteChanged))
}
