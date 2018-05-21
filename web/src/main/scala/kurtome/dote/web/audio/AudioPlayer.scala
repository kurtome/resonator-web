package kurtome.dote.web.audio

import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.radio.RadioStationSchedule
import kurtome.dote.shared.util.observer.Observable
import kurtome.dote.web.audio.Howler.Howl
import kurtome.dote.shared.util.observer.SimpleObservable
import kurtome.dote.web.utils.GlobalNotificationManager
import wvlet.log.LogSupport

import scala.scalajs.js

/**
  * Global audio player.
  */
object AudioPlayer extends LogSupport {
  object PlayerStatuses extends Enumeration {
    val Playing = Value
    val Paused = Value
    val Loading = Value
    val Off = Value
  }
  type PlayerStatus = PlayerStatuses.Value

  object OffSources extends Enumeration {
    val None = Value
    val CloseButton = Value
    val RadioControls = Value
    val RadioPower = Value
  }
  type OffSource = OffSources.Value

  /**
    * State shared with subscribers.
    */
  case class State(status: PlayerStatus,
                   episode: Dotable,
                   stationSchedule: Option[RadioStationSchedule],
                   offSource: OffSource = OffSources.None)

  private var howl: Howl = null
  val stateObservable: Observable[State] = SimpleObservable()
  private var state = State(PlayerStatuses.Off, Dotable.defaultInstance, None)

  def curState = state

  private val handleLoadError: js.Function2[Int, js.Any, Unit] = (soundId, messageOrCode) => {
    warn(s"error loading audio: $messageOrCode")
    if (this.howl != null) {
      this.howl.unload()
      this.howl = null
    }
    GlobalNotificationManager.displayError("Unable to load audio, check internet connection.")
    updateState(curState.copy(PlayerStatuses.Off))
  }

  private val handleLoaded: js.Function0[Unit] = () => {
    resumeOrStartPlaying()
  }

  private def resumeOrStartPlaying() = {
    curState.stationSchedule foreach { station =>
      station.scheduledEpisodes.find(se => {
        val now = js.Date.now()
        se.startTimeMillis <= now && se.endTimeMillis >= now && canPlay(se.getEpisode)
      }) foreach { currentEpisode =>
        val seekSeconds = (js.Date.now() - currentEpisode.startTimeMillis) / 1000.0
        seek(seekSeconds)
      }
    }
    play()
  }

  private val handleStop: js.Function1[Int, Unit] = (id: Int) => {
    if (state.status == PlayerStatuses.Playing) {
      // played through to the end, so reset
      howl.setSeek(0)
      updateState(curState.copy(PlayerStatuses.Paused))

      // try to autoplay the next episode on this station
      curState.stationSchedule.foreach(attemptPlayFromRadioSchedule)
    }
  }

  private def getUrl(episode: Dotable): String = {
    if (episode.kind != Dotable.Kind.PODCAST_EPISODE) {
      ""
    } else {
      episode.getDetails.getPodcastEpisode.getAudio.url.trim
    }
  }

  def canPlay(episode: Dotable): Boolean = {
    getUrl(episode).nonEmpty
  }

  def updateStationSchedule(stationSchedule: RadioStationSchedule) = {
    if (curState.stationSchedule.map(_.getStation).contains(stationSchedule.getStation)) {
      updateState(curState.copy(stationSchedule = Some(stationSchedule)))
    } else {
      warn("cannot update schedule with a station that does not match")
    }
  }

  def attemptPlayFromRadioSchedule(stationSchedule: RadioStationSchedule) = {
    if (curState.stationSchedule.map(_.getStation).contains(stationSchedule.getStation)) {
      debug("already playing this station")
    } else {
      stationSchedule.scheduledEpisodes.find(se => {
        val now = js.Date.now()
        se.startTimeMillis <= now && se.endTimeMillis >= now && canPlay(se.getEpisode)
      }) foreach { episode =>
        if (episode.getEpisode.id != curState.episode.id) {
          startPlayingEpisode(episode.getEpisode, Some(stationSchedule))
        } else {
          resumeOrStartPlaying()
        }
      }

    }
  }

  def startPlayingEpisode(episode: Dotable,
                          stationSchedule: Option[RadioStationSchedule] = None): Unit = {
    if (episode.kind != Dotable.Kind.PODCAST_EPISODE) {
      warn(s"${episode.kind} invalid for playing")
    }

    if (howl != null) {
      howl.stop()
    }

    val url: String = getUrl(episode)
    updateState(State(PlayerStatuses.Loading, episode, stationSchedule))
    howl = Howler.createHowl(src = js.Array[String](url),
                             html5 = true,
                             onstop = handleStop,
                             onloaderror = handleLoadError,
                             onload = handleLoaded)
  }

  def play(): Unit = {
    if (howl != null) {
      howl.play()
      updateState(curState.copy(PlayerStatuses.Playing))
    }
  }

  def off(source: OffSource): Unit = {
    // don't unload if this is just being hidden due to a radio station change (want to keep
    // audio for episodes cached when browsing stations)
    if (source != OffSources.RadioControls && howl != null) {
      howl.unload()
      howl = null
    }
    updateState(
      curState.copy(PlayerStatuses.Off,
                    episode = Dotable.defaultInstance,
                    stationSchedule = None,
                    offSource = source))
  }

  def pause(): Unit = {
    if (howl != null) {
      howl.pause()
      updateState(curState.copy(PlayerStatuses.Paused))
    }
  }

  def rewind(durationSec: Double): Unit = {
    if (howl != null) {
      howl.setSeek(Math.max(0, howl.getSeek() - durationSec))
    }
  }

  def forward(durationSec: Double): Unit = {
    if (howl != null) {
      howl.setSeek(Math.min(howl.getSeek() + durationSec, howl.duration() - 1))
    }
  }

  def seek(seekSeconds: Double): Unit = {
    if (howl != null) {
      howl.setSeek(Math.max(0, Math.min(seekSeconds, howl.duration() - 1)))
    }
  }

  def secondsRemaining: Double = {
    if (howl != null && (state.status == PlayerStatuses.Playing || state.status == PlayerStatuses.Paused)) {
      Math.max(0, howl.duration() - howl.getSeek())
    } else {
      0
    }
  }

  private def updateState(s: State): Unit = {
    if (s != state) {
      state = s
      stateObservable.notifyObservers(state)
    }
  }
}
