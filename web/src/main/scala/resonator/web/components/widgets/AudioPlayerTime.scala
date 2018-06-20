package resonator.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.shared.util.observer.Observer
import resonator.web.audio.AudioPlayer
import resonator.web.audio.AudioPlayer.PlayerStatuses
import org.scalajs.dom
import wvlet.log.LogSupport

object AudioPlayerTime extends LogSupport {

  case class Props()
  case class State(playerState: AudioPlayer.State, secRemaining: Double)

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    var refreshTimerId: Option[Int] = None

    val stateObserver: Observer[AudioPlayer.State] = (state: AudioPlayer.State) => {
      bs.modState(_.copy(playerState = state)).runNow()
    }

    val handleMount: Callback = Callback {
      AudioPlayer.stateObservable.addObserver(stateObserver)
    }

    val handleUnmount: Callback = Callback {
      AudioPlayer.stateObservable.removeObserver(stateObserver)

      refreshTimerId foreach { id =>
        dom.window.clearTimeout(id)
      }
      refreshTimerId = None
    }

    val handleRefresh: () => Unit = () => {
      bs.modState(_.copy(secRemaining = AudioPlayer.secondsRemaining)).runNow()
      refreshTimerId = Some(dom.window.setTimeout(handleRefresh, 500))
    }

    def startStopRefreshTimer() = {
      if (isPlaying) {
        if (refreshTimerId.isEmpty) {
          refreshTimerId = Some(dom.window.setTimeout(handleRefresh, 500))
        }
      } else {
        refreshTimerId foreach { id =>
          dom.window.clearTimeout(id)
        }
        refreshTimerId = None
      }
    }

    def isPlaying: Boolean = bs.state.runNow().playerState.status == PlayerStatuses.Playing

    def render(p: Props, s: State): VdomElement = {
      startStopRefreshTimer()
      val display = if (s.secRemaining == 0) {
        ""
      } else {
        val hours = (s.secRemaining / 3600).toInt
        val minutes = (s.secRemaining / 60).toInt - (hours * 60)
        val seconds = (s.secRemaining % 60).toInt
        if (hours > 0) {
          f"-$hours%02d:$minutes%02d:$seconds%02d"
        } else {
          f"-$minutes%02d:$seconds%02d"
        }
      }
      <.span(display)
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(
      State(playerState = AudioPlayer.curState, secRemaining = AudioPlayer.secondsRemaining))
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .componentWillMount(x => x.backend.handleMount)
    .componentWillUnmount(x => x.backend.handleUnmount)
    .build

  def apply() =
    component.withProps(Props())

}
