package kurtome.dote.web.audio

import dote.proto.api.dotable.Dotable
import kurtome.dote.web.audio.Howler.Howl

import scala.scalajs.js

/**
  * Global audio player.
  */
object AudioPlayer {

  private var currentEpiode: Dotable = Dotable.defaultInstance
  private var howl: Howl = null

  def startPlayingEpisode(episode: Dotable): Unit = {
    if (episode.kind != Dotable.Kind.PODCAST_EPISODE) {
      println("episode invalid for playing")
    }

    if (howl != null) {
      howl.unload()
    }

    currentEpiode = episode
    val url: String = currentEpiode.getDetails.getPodcastEpisode.getAudio.url
    howl = Howler.createHowl(src = js.Array[String](url), html5 = true)
  }

  def play(): Unit = {
    howl.play()
  }
}
