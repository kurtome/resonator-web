package kurtome.dote.web.audio

import com.google.protobuf.duration.Duration
import dote.proto.api.dotable.Dotable
import kurtome.dote.web.audio.AudioPlayer.PlayerStatuses.PlayerStatus
import kurtome.dote.web.audio.Howler.Howl
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.shared.util.observer.Observable
import kurtome.dote.web.shared.util.observer.SimpleObservable
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
    val Off = Value
  }

  private var currentEpiode: Dotable = Dotable.defaultInstance
  private var howl: Howl = null
  val stateObservable: Observable[State] = SimpleObservable()
  var state = State(PlayerStatuses.Off, Dotable.defaultInstance)

  /**
    * State shared with subscribers.
    */
  case class State(status: PlayerStatus, episode: Dotable)

  val handleLoadError: js.Function2[Int, String, Unit] = (soundId, message) => {
    warn(s"error loading audio: $message")
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
    howl =
      Howler.createHowl(src = js.Array[String](url), html5 = true, onloaderror = handleLoadError)
    play()
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
