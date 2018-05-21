package kurtome.dote.web.audio

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

/**
  * API for https://github.com/goldfire/howler.js#documentation
  *
  * Global methods are on the [[kurtome.dote.web.audio.Howler.Global]] object, everything else is
  * via creating a [[kurtome.dote.web.audio.Howler.Howl]] object.
  */
object Howler {

  @JSImport("howler", JSImport.Default)
  @js.native
  object Global extends js.Object {

    /**
      * Mute or unmute all sounds.
      */
    def mute(muted: Boolean): Unit = js.native

    /**
      * Get/set the global volume for all sounds, relative to their own volume.
      *
      * @param volume Volume from 0.0 to 1.0.
      */
    def volume(volume: js.UndefOr[Double]): Unit = js.native

    /**
      * Check supported audio codecs. Returns true if the codec is supported in the current browser.
      *
      * @param ext File extension. One of: "mp3", "mpeg", "opus", "ogg", "oga", "wav", "aac", "caf",
      *            "m4a", "mp4", "weba", "webm", "dolby", "flac".
      */
    def codecs(ext: String): Boolean = js.native

    /**
      * Unload and destroy all currently loaded Howl objects. This will immediately stop all sounds
      * and remove them from cache.
      */
    def unload(): Unit = js.native
  }

  def createHowl(src: js.Array[String],
                 volume: js.UndefOr[Double] = js.undefined,
                 html5: js.UndefOr[Boolean] = js.undefined,
                 preload: js.UndefOr[Boolean] = js.undefined,
                 autoplay: js.UndefOr[Boolean] = js.undefined,
                 mute: js.UndefOr[Boolean] = js.undefined,
                 sprite: js.UndefOr[js.Object] = js.undefined,
                 rate: js.UndefOr[Double] = js.undefined,
                 pool: js.UndefOr[Int] = js.undefined,
                 loop: js.UndefOr[Boolean] = js.undefined,
                 format: js.UndefOr[js.Array[String]] = js.undefined,
                 xhrWithCredentials: js.UndefOr[js.Array[String]] = js.undefined,
                 onload: js.UndefOr[js.Function0[Unit]] = js.undefined,
                 onloaderror: js.UndefOr[js.Function2[Int, js.Any, Unit]] = js.undefined,
                 onplayerror: js.UndefOr[js.Function2[Int, String, Unit]] = js.undefined,
                 onplay: js.UndefOr[js.Function1[Int, Unit]] = js.undefined,
                 onpause: js.UndefOr[js.Function1[Int, Unit]] = js.undefined,
                 onstop: js.UndefOr[js.Function1[Int, Unit]] = js.undefined,
                 onmute: js.UndefOr[js.Function1[Int, Unit]] = js.undefined,
                 onvolume: js.UndefOr[js.Function1[Int, Unit]] = js.undefined,
                 onrate: js.UndefOr[js.Function1[Int, Unit]] = js.undefined,
                 onseek: js.UndefOr[js.Function1[Int, Unit]] = js.undefined,
                 onfade: js.UndefOr[js.Function1[Int, Unit]] = js.undefined): Howl = {
    val options = new js.Object().asInstanceOf[HowlOptions]
    options.src = src
    options.volume = volume
    options.html5 = html5
    options.preload = preload
    options.autoplay = autoplay
    options.mute = mute
    options.sprite = sprite
    options.rate = rate
    options.pool = pool
    options.loop = loop
    options.format = format
    options.xhrWithCredentials = xhrWithCredentials
    options.onload = onload
    options.onloaderror = onloaderror
    options.onplayerror = onplayerror
    options.onplay = onplay
    options.onpause = onpause
    options.onstop = onstop
    options.onmute = onmute
    options.onvolume = onvolume
    options.onrate = onrate
    options.onseek = onseek
    options.onfade = onfade

    new Howl(options)
  }

  /**
    * Options for creating a new [[Howl]]
    */
  @js.native
  trait HowlOptions extends js.Object {

    /**
      * The sources to the track(s) to be loaded for the sound (URLs or base64 data URIs). These
      * should be in order of preference, howler.js will automatically load the first one that is
      * compatible with the current browser. If your files have no extensions, you will need to
      * explicitly specify the extension using the format property.
      */
    var src: js.Array[String] = js.native

    /**
      * The volume of the specific track, from 0.0 to 1.0.
      */
    var volume: js.UndefOr[Double] = js.native

    /**
      * Set to true to force HTML5 Audio. This should be used for large audio files so that you
      * don't have to wait for the full file to be downloaded and decoded before playing.
      */
    var html5: js.UndefOr[Boolean] = js.native

    /**
      * Automatically begin downloading the audio file when the Howl is defined.
      */
    var preload: js.UndefOr[Boolean] = js.native

    /**
      * Set to true to automatically start playback when sound is loaded.
      */
    var autoplay: js.UndefOr[Boolean] = js.native

    /**
      * Set to true to load the audio muted.
      */
    var mute: js.UndefOr[Boolean] = js.native

    /**
      * Define a sound sprite for the sound. The offset and duration are defined in milliseconds
      * A third (optional) parameter is available to set a sprite as looping. An easy way to
      * generate compatible sound sprites is with audiosprite.
      */
    var sprite: js.UndefOr[js.Object] = js.native

    /**
      * The rate of playback. 0.5 to 4.0, with 1.0 being normal speed.
      */
    var rate: js.UndefOr[Double] = js.native

    /**
      * The size of the inactive sounds pool. Once sounds are stopped or finish playing, they are
      * marked as ended and ready for cleanup. We keep a pool of these to recycle for improved
      * performance. Generally this doesn't need to be changed. It is important to keep in mind that
      * when a sound is paused, it won't be removed from the pool and will still be considered
      * active so that it can be resumed later.
      */
    var pool: js.UndefOr[Int] = js.native

    /**
      * Set to true to automatically loop the sound forever.
      */
    var loop: js.UndefOr[Boolean] = js.native

    /**
      * howler.js automatically detects your file format from the extension, but you may also
      * specify a format in situations where extraction won't work (such as with a SoundCloud
      * stream).
      */
    var format: js.UndefOr[js.Array[String]] = js.native

    /**
      * Whether or not to enable the withCredentials flag on XHR requests used to fetch audio files
      * when using Web Audio API (see reference).
      */
    var xhrWithCredentials: js.UndefOr[js.Array[String]] = js.native

    /**
      * Fires when the sound is loaded.
      */
    var onload: js.UndefOr[js.Function0[Unit]] = js.native

    /**
      * Fires when the sound is unable to load. The first parameter is the ID of the sound (if it
      * exists) and the second is the error message/code.
      */
    var onloaderror: js.UndefOr[js.Function2[Int, js.Any, Unit]] = js.native

    /**
      * Fires when the sound is unable to play. The first parameter is the ID of the sound and the
      * second is the error message/code.
      */
    var onplayerror: js.UndefOr[js.Function2[Int, String, Unit]] = js.native

    /**
      * Fires when the sound begins playing. The first parameter is the ID of the sound.
      */
    var onplay: js.UndefOr[js.Function1[Int, Unit]] = js.native

    /**
      * Fires when the sound has been paused. The first parameter is the ID of the sound.
      */
    var onpause: js.UndefOr[js.Function1[Int, Unit]] = js.native

    /**
      * Fires when the sound has been stopped. The first parameter is the ID of the sound.
      */
    var onstop: js.UndefOr[js.Function1[Int, Unit]] = js.native

    /**
      * Fires when the sound has been muted/unmuted. The first parameter is the ID of the sound.
      */
    var onmute: js.UndefOr[js.Function1[Int, Unit]] = js.native

    /**
      * Fires when the sound's volume has changed. The first parameter is the ID of the sound.
      */
    var onvolume: js.UndefOr[js.Function1[Int, Unit]] = js.native

    /**
      * Fires when the sound's playback rate has changed. The first parameter is the ID of the
      * sound.
      */
    var onrate: js.UndefOr[js.Function1[Int, Unit]] = js.native

    /**
      * Fires when the sound has been seeked. The first parameter is the ID of the sound.
      */
    var onseek: js.UndefOr[js.Function1[Int, Unit]] = js.native

    /**
      * Fires when the current sound finishes fading in/out. The first parameter is the ID of the
      * sound.
      */
    var onfade: js.UndefOr[js.Function1[Int, Unit]] = js.native

  }

  @JSImport("howler", "Howl")
  @js.native
  class Howl(val howlOptions: HowlOptions) extends js.Object {

    /**
      * Begins playback of a sound.
      * @param id Takes one parameter that can either be a sprite or sound ID. If a sprite is
      *           passed, a new sound will play based on the sprite's definition. If a sound ID is
      *           passed, the previously played sound will be played (for example, after pausing
      *           it). However, if an ID of a sound that has been drained from the pool is passed,
      *           nothing will play.
      * @return the sound id to be used with other methods. Only method that can't be chained.
      */
    def play(id: js.UndefOr[Int] = js.undefined): Int = js.native

    /**
      * Pauses playback of sound or group, saving the seek of playback.
      * @param id The sound ID. If none is passed, all sounds in group are paused.
      * @return this instance
      */
    def pause(id: js.UndefOr[Int] = js.undefined): Howl = js.native

    /**
      * Stops playback of sound, resetting seek to 0.
      * @param id The sound ID. If none is passed, all sounds in group are stopped.
      * @return this instance
      */
    def stop(id: js.UndefOr[Int] = js.undefined): Howl = js.native

    /**
      * Mutes the sound, but doesn't pause the playback.
      * @param muted True to mute and false to unmute.
      * @param id The sound ID. If none is passed, all sounds in group are muted.
      * @return this instance
      */
    def mute(muted: Boolean, id: js.UndefOr[Int] = js.undefined): Howl = js.native

    /**
      * Get/set volume of this sound or the group. This method optionally takes 0, 1 or 2 arguments.
      * @param volume Volume from 0.0 to 1.0.
      * @param id The sound ID. If none is passed, all sounds in group have volume altered relative
      *           to their own volume.
      * @return this instance
      */
    def volume(volume: Double, id: js.UndefOr[Int] = js.undefined): Howl = js.native

    /**
      * Fade a currently playing sound between two volumes. Fires the fade event when complete.
      * @param from Volume to fade from (0.0 to 1.0).
      * @param to Volume to fade from (0.0 to 1.0).
      * @param duration Time in milliseconds to fade.
      * @param id The sound ID. If none is passed, all sounds in group will fade.
      * @return this instance
      */
    def fade(from: Double,
             to: Double,
             duration: Double,
             id: js.UndefOr[Int] = js.undefined): Howl =
      js.native

    /**
      * Get/set the rate of playback for a sound. This method optionally takes 0, 1 or 2 arguments.
      * @param rate The rate of playback. 0.5 to 4.0, with 1.0 being normal speed.
      * @param id The sound ID. If none is passed, playback rate of all sounds in group will change.
      * @return this instance
      */
    def rate(rate: Double, id: js.UndefOr[Int] = js.undefined): Howl = js.native

    /**
      * Get/set the position of playback for a sound.
      * @param unused DO NOT SET THIS PARAM (it becomes a setSeek then)
      * @param id The sound ID. If none is passed, get for the first sound
      * @return the current seek of the first sound
      */
    @JSName("seek")
    def getSeek(unused: js.UndefOr[Double] = js.undefined,
                id: js.UndefOr[Int] = js.undefined): Double = js.native

    /**
      * Get/set the position of playback for a sound.
      * @param seek The position to move current playback to (in seconds).
      * @param id The sound ID. If none is passed, the first sound will seek.
      * @return this instance
      */
    @JSName("seek")
    def setSeek(seek: Double, id: js.UndefOr[Int] = js.undefined): Howl = js.native

    /**
      * Get/set whether to loop the sound or group. This method can optionally take 0, 1 or 2 arguments.
      * @param loop To loop or not to loop, that is the question.
      * @param id The sound ID. If none is passed, all sounds in group will have their loop property updated.
      * @return this instance
      */
    def loop(loop: Boolean, id: js.UndefOr[Int] = js.undefined): Howl = js.native

    /**
      * Check the load status of the Howl, returns a unloaded, loading or loaded.
      */
    def state(): String = js.native

    /**
      * Check if a sound is currently playing or not, returns a Boolean. If no sound ID is passed,
      * check if any sound in the Howl group is playing.
      * @param id The sound ID top check.
      */
    def playing(id: js.UndefOr[Int] = js.undefined): Boolean = js.native

    /**
      * Get the duration of the audio source. Will return 0 until after the load event fires.
      * @param id The sound ID to check. Passing an ID will return the duration of the sprite being
      *           played on this instance; otherwise, the full source duration is returned.
      */
    def duration(id: js.UndefOr[Int] = js.undefined): Double = js.native

    /**
      * Listen for events. Multiple events can be added by calling this multiple times.
      * @param event Name of event to fire/set (load, loaderror, playerror, play, end, pause, stop,
      *              mute, volume, rate, seek, fade).
      * @param callback Define function to fire on event.
      * @param id Only listen to events for this sound id.
      * @return this instance
      */
    def on(event: String,
           callback: js.Function0[Unit],
           id: js.UndefOr[Int] = js.undefined): Double = js.native

    /**
      * Same as [[on]], but it removes itself after the callback is fired.
      * @param event Name of event to fire/set (load, loaderror, playerror, play, end, pause, stop,
      *              mute, volume, rate, seek, fade).
      * @param callback Define function to fire on event.
      * @param id Only listen to events for this sound id.
      * @return this instance
      */
    def once(event: String,
             callback: js.Function0[Unit],
             id: js.UndefOr[Int] = js.undefined): Double = js.native

    /**
      * Remove event listener that you've set. Call without parameters to remove all events.
      * @param event Name of event to fire/set (load, loaderror, playerror, play, end, pause, stop,
      *              mute, volume, rate, seek, fade).
      * @param callback The listener to remove. Omit this to remove all events of type.
      * @param id Only remove events for this sound id.
      * @return this instance
      */
    def off(event: String,
            callback: js.Function0[Unit],
            id: js.UndefOr[Int] = js.undefined): Double = js.native

    /**
      * This is called by default, but if you set preload to false, you must call load before you
      * can play any sounds.
      * @return this instance
      */
    def load(): Howl = js.native

    /**
      * Unload and destroy a Howl object. This will immediately stop all sounds attached to this
      * sound and remove it from the cache.
      * @return this instance
      */
    def unload(): Howl = js.native
  }

}
