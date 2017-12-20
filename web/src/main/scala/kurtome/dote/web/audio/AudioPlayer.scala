package kurtome.dote.web.audio

import com.google.protobuf.duration.Duration
import dote.proto.api.dotable.Dotable
import kurtome.dote.web.audio.AudioPlayer.PlayerStatuses.PlayerStatus
import kurtome.dote.web.audio.Howler.Howl
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.shared.util.observer.Observable
import kurtome.dote.web.shared.util.observer.SimpleObservable
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

  private val handleLoadError: js.Function2[Int, Int, Unit] = (soundId, messageId) => {
    warn(s"error loading audio: $messageId")
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

  def startPlayingEpisode(episode: Dotable): Unit = {
    if (episode.kind != Dotable.Kind.PODCAST_EPISODE) {
      warn(s"${episode.kind} invalid for playing")
    }

    if (howl != null) {
      howl.unload()
    }

    currentEpiode = episode
    val url: String = currentEpiode.getDetails.getPodcastEpisode.getAudio.url
    updateState(State(PlayerStatuses.Loading, currentEpiode))
    howl = Howler.createHowl(src = js.Array[String](url),
                             html5 = true,
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
      howl.stop()
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
      howl.setSeek(Math.min(howl.getSeek() + durationSec, howl.duration()))
    }
  }

  private def updateState(s: State): Unit = {
    state = s
    stateObservable.notifyObservers(state)
  }

  def status: PlayerStatus = {
    if (howl == null) {
      PlayerStatuses.Paused
    } else {
      if (howl.playing()) {
        PlayerStatuses.Playing
      } else {
        PlayerStatuses.Paused
      }
    }
  }
}
