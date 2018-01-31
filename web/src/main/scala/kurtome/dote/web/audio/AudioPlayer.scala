package kurtome.dote.web.audio

import com.google.protobuf.duration.Duration
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.shared.util.observer.{Observable, SimpleObservable}
import kurtome.dote.web.audio.AudioPlayer.PlayerStatuses.PlayerStatus
import kurtome.dote.web.audio.Howler.Howl
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.shared.util.observer.SimpleObservable
import kurtome.dote.web.utils.GlobalNotificationManager
import org.scalajs.dom.raw.{HTMLMediaElement, MediaError}
import wvlet.log.LogSupport

import scala.scalajs.js

/**
  * Global audio player.
  */
object AudioPlayer extends LogSupport {
  object PlayerStatuses extends Enumeration {
    type PlayerStatus = Value
    val Playing = Value
    val Paused = Value
    val Loading = Value
    val Off = Value
  }

  private var currentEpiode: Dotable = Dotable.defaultInstance
  private var howl: Howl = null
  val stateObservable: Observable[State] = SimpleObservable()
  private var state = State(PlayerStatuses.Off, Dotable.defaultInstance)

  def curState = state

  /**
    * State shared with subscribers.
    */
  case class State(status: PlayerStatus, episode: Dotable)

  private val handleLoadError: js.Function2[Int, js.Any, Unit] = (soundId, messageOrCode) => {
    warn(s"error loading audio: $messageOrCode")
    if (this.howl != null) {
      this.howl.unload()
      this.howl = null
    }
    GlobalNotificationManager.displayMessage("Unable to load audio, check internet connection.")
    updateState(State(PlayerStatuses.Off, currentEpiode))
  }

  private val handleLoaded: js.Function0[Unit] = () => {
    play()
  }

  private val handleStop: js.Function1[Int, Unit] = (id: Int) => {
    if (state.status == PlayerStatuses.Playing) {
      // played through to the end, so reset
      howl.setSeek(0)
      updateState(State(PlayerStatuses.Paused, currentEpiode))
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

  def startPlayingEpisode(episode: Dotable): Unit = {
    if (episode.kind != Dotable.Kind.PODCAST_EPISODE) {
      warn(s"${episode.kind} invalid for playing")
    }

    if (howl != null) {
      howl.unload()
    }

    currentEpiode = episode
    val url: String = getUrl(currentEpiode)
    updateState(State(PlayerStatuses.Loading, currentEpiode))
    howl = Howler.createHowl(src = js.Array[String](url),
                             html5 = true,
                             onstop = handleStop,
                             onloaderror = handleLoadError,
                             onload = handleLoaded)
  }

  def play(): Unit = {
    if (howl != null) {
      howl.play()
      updateState(State(PlayerStatuses.Playing, currentEpiode))
    }
  }

  def off(): Unit = {
    if (howl != null) {
      howl.unload()
    }
    updateState(State(PlayerStatuses.Off, currentEpiode))
  }

  def pause(): Unit = {
    if (howl != null) {
      howl.pause()
      updateState(State(PlayerStatuses.Paused, currentEpiode))
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

  def secondsRemaining: Double = {
    if (howl != null && (state.status == PlayerStatuses.Playing || state.status == PlayerStatuses.Paused)) {
      Math.max(0, howl.duration() - howl.getSeek())
    } else {
      0
    }
  }

  private def updateState(s: State): Unit = {
    state = s
    stateObservable.notifyObservers(state)
  }
}
