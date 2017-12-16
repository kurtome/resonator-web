package kurtome.dote.web.components.widgets

import dote.proto.api.dotable.Dotable
import kurtome.dote.web.DoteRoutes.DoteRouterCtl
import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.raw.SyntheticEvent
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.DoteRoutes.{DoteRouterCtl, PodcastRoute}
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.audio.AudioPlayer
import kurtome.dote.web.audio.AudioPlayer.PlayerStatuses
import kurtome.dote.web.components.ComponentHelpers
import kurtome.dote.web.shared.util.observer.Observer
import kurtome.dote.web.utils.MuiInlineStyleSheet

import scala.scalajs.js
import scalacss.internal.mutable.StyleSheet

object AudioControls {

  val bottomNavHeight = 56
  val controlsHeight = 80
  val controlsWidth = 300

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val playerWrapper = style(
      width(100 %%),
      bottom(bottomNavHeight + 16 px),
      position.fixed
    )

    val playerRoot = style(
      width(controlsWidth px),
      marginLeft(16 px),
      height(controlsHeight px)
    )

    val contentWrapper = style(
      position.relative
    )

    val tileWrapper = style(
      float.left
    )

    val waveWrapper = style(
      top(0 px),
      position.absolute,
      display.inlineBlock
    )

    val buttonSpaceWrapper = style(
      pointerEvents := "none",
      position.absolute,
      display.inlineBlock
    )

    val buttonGrid = style(
      width(100 %%),
      height(100 %%)
    )

    val bottomButton = style(
      pointerEvents := "auto",
      marginLeft(8 px),
      marginBottom(-6 px)
    )

    val closeButton = style(
      pointerEvents := "auto",
      fontSize.larger,
      margin(-10 px)
    )

    val titleTextWrapper = style(
      width(75 %%),
      marginLeft(8 px),
      marginTop(8 px)
    )

    val titleText = style(
      whiteSpace.nowrap,
      textOverflow := "ellipsis",
      overflow.hidden
    )

  }
  Styles.addToDocument()
  val muiStyles = new MuiInlineStyleSheet(Styles)
  import muiStyles._

  case class Props(routerCtl: DoteRouterCtl)
  case class State(playerState: AudioPlayer.State)

  class Backend(bs: BackendScope[Props, State]) {

    val stateObserver: Observer[AudioPlayer.State] = (state: AudioPlayer.State) => {
      bs.modState(_.copy(playerState = state)).runNow()
      bs.forceUpdate.runNow()
    }
    AudioPlayer.stateObservable.addObserver(stateObserver)
    val onUnmount: Callback = Callback {
      AudioPlayer.stateObservable.removeObserver(stateObserver)
    }

    def isPlaying: Boolean = bs.state.runNow().playerState.status == PlayerStatuses.Playing

    val playPauseClicked = Callback {
      if (isPlaying) {
        AudioPlayer.pause()
      } else {
        AudioPlayer.play()
      }
    }

    val offClicked = Callback {
      AudioPlayer.off()
    }

    val rewind10Clicked = Callback {
      AudioPlayer.rewind(10)
    }

    val forward30Clicked = Callback {
      AudioPlayer.forward(30)
    }

    def render(p: Props, s: State): VdomElement = {
      val tileWidth = controlsHeight
      val buttonSpaceWidth = controlsWidth - tileWidth

      val shouldCenter = ComponentHelpers.currentBreakpointString == "xs"

      if (s.playerState.status == PlayerStatuses.Off) {
        <.div()
      } else {
        Grid(container = true,
             style = Styles.playerWrapper.inline,
             justify = if (shouldCenter) Grid.Justify.Center else Grid.Justify.FlexStart)(
          Grid(item = true)(
            Paper(elevation = 8, style = Styles.playerRoot.inline)(
              <.div(
                ^.className := Styles.contentWrapper,
                <.div(
                  ^.className := Styles.tileWrapper,
                  EntityTile(routerCtl = p.routerCtl,
                             dotable = s.playerState.episode,
                             elevation = 0,
                             width = asPxStr(controlsHeight))()
                ),
                <.div(
                  ^.className := Styles.waveWrapper,
                  AudioWave(width = asPxStr(buttonSpaceWidth),
                            height = asPxStr(tileWidth),
                            started = isPlaying)
                ),
                <.div(
                  ^.width := asPxStr(buttonSpaceWidth),
                  ^.height := asPxStr(controlsHeight),
                  ^.className := Styles.buttonSpaceWrapper,
                  Grid(container = true,
                       spacing = 0,
                       justify = Grid.Justify.Center,
                       alignItems = Grid.AlignItems.FlexEnd,
                       style = Styles.buttonGrid.inline)(
                    Grid(item = true)(
                      IconButton(style = Styles.bottomButton.inline, onClick = rewind10Clicked)(
                        Icons.Replay10()
                      ),
                      IconButton(style = Styles.bottomButton.inline, onClick = playPauseClicked)(
                        if (isPlaying) {
                          Icons.Pause()
                        } else {
                          Icons.PlayArrow()
                        }
                      ),
                      IconButton(style = Styles.bottomButton.inline, onClick = forward30Clicked)(
                        Icons.Forward30()
                      )
                    )
                  )
                ),
                <.div(
                  ^.width := asPxStr(buttonSpaceWidth),
                  ^.height := asPxStr(controlsHeight),
                  ^.className := Styles.buttonSpaceWrapper,
                  Grid(container = true,
                       spacing = 0,
                       justify = Grid.Justify.SpaceBetween,
                       alignItems = Grid.AlignItems.FlexStart,
                       style = Styles.buttonGrid.inline)(
                    Grid(item = true, style = Styles.titleTextWrapper.inline)(
                      Typography(
                        typographyType = Typography.Type.Caption,
                        style = Styles.titleText.inline)(s.playerState.episode.getCommon.title)
                    ),
                    Grid(item = true)(
                      IconButton(style = Styles.closeButton.inline, onClick = offClicked)(
                        Icons.Close()
                      )
                    )
                  )
                )
              )
            )
          )
        )
      }
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State(playerState = AudioPlayer.state))
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .componentWillUnmount(x => x.backend.onUnmount)
    .build

  def apply(routerCtl: DoteRouterCtl) =
    component.withProps(Props(routerCtl))

}
