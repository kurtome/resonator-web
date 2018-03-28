package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.util.observer.Observer
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.audio.AudioPlayer
import kurtome.dote.web.audio.AudioPlayer.PlayerStatuses
import kurtome.dote.web.components.ComponentHelpers
import kurtome.dote.web.components.widgets.card.PodcastImageCard
import kurtome.dote.web.utils.BaseBackend
import wvlet.log.LogSupport

object AudioControls extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val playerWrapper = style(
      width(100 %%),
      bottom(bottomNavHeight + 16 px),
      position.fixed
    )

    val progressWrapper = style(
      width(controlsWidth px),
      marginLeft(16 px)
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
      fontSize(0.9 rem),
      lineHeight(1 rem),
      whiteSpace.nowrap,
      textOverflow := "ellipsis",
      overflow.hidden
    )

    val timeText = style(
      fontSize(0.75 rem),
      lineHeight(0.9 rem),
      whiteSpace.nowrap,
      textOverflow := "ellipsis",
      overflow.hidden
    )

  }

  val bottomNavHeight = 56
  val controlsHeight = 80
  val controlsWidth = Math.min(400, ContentFrame.innerWidthPx)

  case class Props()
  case class State(playerState: AudioPlayer.State)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val stateObserver: Observer[AudioPlayer.State] = (state: AudioPlayer.State) => {
      bs.modState(_.copy(playerState = state)).runNow()
    }
    AudioPlayer.stateObservable.addObserver(stateObserver)
    val onUnmount: Callback = Callback {
      AudioPlayer.stateObservable.removeObserver(stateObserver)
    }

    def isPlaying: Boolean = bs.state.runNow().playerState.status == PlayerStatuses.Playing

    def isLoading: Boolean = bs.state.runNow().playerState.status == PlayerStatuses.Loading

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
             style = Styles.playerWrapper,
             justify = if (shouldCenter) Grid.Justify.Center else Grid.Justify.FlexStart)(
          Grid(item = true)(
            <.div(
              ^.className := Styles.progressWrapper,
              Fader(in = isLoading)(LinearProgress()())
            ),
            Paper(elevation = 8, style = Styles.playerRoot)(
              <.div(
                ^.className := Styles.contentWrapper,
                <.div(
                  ^.className := Styles.tileWrapper,
                  doteRouterCtl.link(
                    DetailsRoute(s.playerState.episode.id, s.playerState.episode.slug))(
                    PodcastImageCard(dotable = s.playerState.episode,
                                     width = asPxStr(controlsHeight))()
                  )
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
                       style = Styles.buttonGrid)(
                    Grid(item = true)(
                      IconButton(style = Styles.bottomButton, onClick = rewind10Clicked)(
                        Icons.Replay10()
                      ),
                      IconButton(style = Styles.bottomButton, onClick = playPauseClicked)(
                        if (isPlaying) {
                          Icons.Pause()
                        } else {
                          Icons.PlayArrow()
                        }
                      ),
                      IconButton(style = Styles.bottomButton, onClick = forward30Clicked)(
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
                       style = Styles.buttonGrid)(
                    Grid(item = true, style = Styles.titleTextWrapper)(
                      Typography(variant = Typography.Variants.Body2, style = Styles.titleText)(
                        s.playerState.episode.getCommon.title),
                      Typography(variant = Typography.Variants.Caption, style = Styles.timeText)(
                        AudioPlayerTime()())
                    ),
                    Grid(item = true)(
                      IconButton(style = Styles.closeButton, onClick = offClicked)(
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
    .initialState(State(playerState = AudioPlayer.curState))
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .componentWillUnmount(x => x.backend.onUnmount)
    .build

  def apply() = component.withProps(Props())

}
