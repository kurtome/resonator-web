package kurtome.dote.web.components.widgets

import kurtome.dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.shared.constants.StringValues
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.widgets.button.emote._
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.scalajs.js

object PodcastTile extends LogSupport {

  private object Animations extends StyleSheet.Inline {
    import dsl._
    val fadeInImage = keyframes(
      (0 %%) -> keyframe(opacity(0)),
      (100 %%) -> keyframe(opacity(1))
    )
  }
  Animations.addToDocument()

  private object Styles extends StyleSheet.Inline with MuiInlineStyleSheet {
    import dsl._

    val wrapper = style(
      position.relative
    )

    val container = style(
      position.absolute,
      pointerEvents := "auto"
    )

    val imageContainer = style(
      )

    val overlayContainer = style(
      position.absolute,
      pointerEvents := "none",
      width(100 %%),
      height(100 %%)
    )

    val overlayActionsContainer = style(
      width(100 %%),
      height(100 %%)
    )

    val overlay = style(
      position.absolute,
      backgroundColor(rgba(255, 255, 255, 0.4)),
      width(100 %%),
      height(100 %%)
    )

    val nestedImg = style(
      position.absolute,
      animation := s"${Animations.fadeInImage.name.value} 1s",
      width(100 %%)
    )

    val placeholder = style(
      position.absolute,
      backgroundColor(rgb(200, 200, 200)),
      width(100 %%),
      // Use padding top to force the height of the div to match the width
      paddingTop(100 %%)
    )
  }
  Styles.addToDocument()
  import Styles.richStyle

  case class Props(routerCtl: DoteRouterCtl,
                   dotable: Dotable,
                   elevation: Int,
                   width: String,
                   disableActions: Boolean)
  case class State(imgLoaded: Boolean = false,
                   hover: Boolean = false,
                   smileCount: Int = 0,
                   cryCount: Int = 0,
                   laughCount: Int = 0,
                   scowlCount: Int = 0)

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    val sendDoteToServer: js.Function0[Unit] = Debounce.debounce0(waitMs = 2000) { () =>
      val p: Props = bs.props.runNow()
      val s: State = bs.state.runNow()
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

    val handleSadValueChanged = (value: Int) =>
      Callback {
        bs.modState(s => s.copy(cryCount = value))
        sendDoteToServer()
    }

    val handleLaughValueChanged = (value: Int) =>
      Callback {
        bs.modState(s => s.copy(laughCount = value))
        sendDoteToServer()
    }

    val handleScowlValueChanged = (value: Int) =>
      Callback {
        bs.modState(s => s.copy(scowlCount = value))
        sendDoteToServer()
    }

    def render(p: Props, s: State): VdomElement = {
      val id = p.dotable.id
      val slug = p.dotable.slug
      val detailRoute = DetailsRoute(id = id, slug = slug)

      val url = if (p.dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
        p.dotable.getRelatives.getParent.getDetails.getPodcast.imageUrl
      } else {
        p.dotable.getDetails.getPodcast.imageUrl
      }

      val showActions = !p.disableActions && s.hover && LoggedInPersonManager.isLoggedIn

      Paper(elevation = if (s.hover) p.elevation * 2 else p.elevation,
            className = SharedStyles.inlineBlock)(
        <.div(
          ^.className := Styles.wrapper,
          ^.width := p.width,
          ^.height := p.width,
          ^.onMouseEnter --> bs.modState(_.copy(hover = true)),
          ^.onMouseLeave --> bs.modState(_.copy(hover = false)),
          p.routerCtl.link(detailRoute)(
            ^.className := Styles.container,
            EntityImage(routerCtl = p.routerCtl, dotable = p.dotable, width = p.width)()
          ),
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
                        CryButton(s.cryCount, onValueChanged = handleLikeValueChanged)())
                    )),
                  Grid(item = true)(
                    Grid(container = true, spacing = 0, justify = Grid.Justify.SpaceBetween)(
                      Grid(item = true)(
                        LaughButton(s.laughCount, onValueChanged = handleLaughValueChanged)()),
                      Grid(item = true)(
                        ScowlButton(s.scowlCount, onValueChanged = handleScowlValueChanged)())
                    ))
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

  def apply(routerCtl: DoteRouterCtl,
            dotable: Dotable,
            elevation: Int = 6,
            width: String = "175px",
            disableActions: Boolean = false) =
    component.withProps(Props(routerCtl, dotable, elevation, width, disableActions))
}
