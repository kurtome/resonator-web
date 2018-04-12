package kurtome.dote.web.components.widgets.card

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
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
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable, active: Boolean) =
    component.withProps(Props(dotable, active))
}
