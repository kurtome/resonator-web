package kurtome.dote.web.audio

import kurtome.dote.proto.api.action.get_radio_schedule.GetRadioScheduleRequest
import kurtome.dote.proto.api.action.get_radio_schedule.GetRadioScheduleResponse
import kurtome.dote.proto.api.radio.RadioStation.FrequencyKind
import kurtome.dote.proto.api.radio.RadioStationSchedule
import kurtome.dote.shared.util.observer.Observable
import kurtome.dote.shared.util.observer.Observer
import kurtome.dote.shared.util.observer.SimpleObservable
import kurtome.dote.web.audio.AudioPlayer.OffSources
import kurtome.dote.web.audio.AudioPlayer.PlayerStatuses
import kurtome.dote.web.rpc.ResonatorApiClient
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

/**
  * Controller for the global radio tuner, viewed at /tuner
  */
object RadioTuner extends LogSupport {

  /**
    * State shared with subscribers.
    */
  case class State(on: Boolean,
                   frequency: Float,
                   frequencyKind: FrequencyKind,
                   amSchedule: Seq[RadioStationSchedule] = Nil,
                   fmSchedule: Seq[RadioStationSchedule] = Nil) {

    def off: Boolean = !on

    def currentSchedules: Seq[RadioStationSchedule] = {
      frequencyKind match {
        case FrequencyKind.FM => fmSchedule
        case _ => amSchedule
      }
    }

    def currentFrequencyRange: Range = {
      frequencyKind match {
        case FrequencyKind.FM => fmFrequencyRange
        case _ => amFrequencyRange
      }
    }

  }

  val amFrequencyRange = Range(535, 1605)
  val fmFrequencyRange = Range(88, 108)

  private val switchUrl = "/assets/audio/radioSwitch1.mp3"
  private val switchHowl = Howler.createHowl(src = js.Array[String](switchUrl),
                                             volume = 0.5,
                                             html5 = true,
                                             autoplay = false,
                                             preload = true)
  private val staticUrl = "/assets/audio/static1.mp3"
  private val staticHowl = Howler.createHowl(src = js.Array[String](staticUrl),
                                             html5 = true,
                                             autoplay = false,
                                             preload = true,
                                             loop = true)

  val stateObservable: Observable[State] = SimpleObservable()
  private var state = State(on = false, frequency = 0, frequencyKind = FrequencyKind.AM)
  private var fetchTimerId: Option[Long] = None

  def curState = state

  private val audioPlayerStateObserver: Observer[AudioPlayer.State] =
    (audioPlayerState: AudioPlayer.State) => {
      if (audioPlayerState.status == PlayerStatuses.Off && audioPlayerState.offSource == OffSources.CloseButton) {
        power(false)
        pauseStatic()
      } else {
        if (curState.on && audioPlayerState.status != PlayerStatuses.Playing) {
          playStatic()
        } else if (audioPlayerState.status == PlayerStatuses.Playing) {
          pauseStatic()
        }
      }
    }
  AudioPlayer.stateObservable.addObserver(audioPlayerStateObserver)

  def setSchedule(response: GetRadioScheduleResponse): Unit = {
    updateState(state.copy(amSchedule = response.amStations, fmSchedule = response.fmStations))

    AudioPlayer.curState.stationSchedule foreach { playerStationSchedule =>
      (state.amSchedule ++ state.fmSchedule)
        .find(_.getStation == playerStationSchedule.getStation) foreach { updatedSchedule =>
        AudioPlayer.updateStationSchedule(updatedSchedule)
      }
    }

    if (state.frequency == 0) {
      randomOption(state.currentSchedules).foreach(stationSchedule =>
        setFrequency(frequency = stationSchedule.getStation.frequency))
    }
  }

  def setStation(band: FrequencyKind, frequency: Float) = {
    updateState(state.copy(frequencyKind = band, frequency = frequency))
  }

  def setFrequency(frequency: Float): Unit = {
    updateState(curState.copy(frequency = frequency))
    syncAudioPlayer()
  }

  def power(on: Boolean): Unit = {
    switchHowl.play()
    updateState(curState.copy(on = on))
    syncAudioPlayer()
  }

  def seekBackward(): Unit = {
    val nextFrequency: Float = state.currentSchedules.reverse find { stationSchedule =>
      stationSchedule.getStation.frequency < state.frequency
    } map (_.getStation.frequency) getOrElse state.frequency
    setFrequency(nextFrequency)
  }

  def seekForward(): Unit = {
    val nextFrequency: Float = state.currentSchedules find { stationSchedule =>
      stationSchedule.getStation.frequency > state.frequency
    } map (_.getStation.frequency) getOrElse state.frequency
    setFrequency(nextFrequency)
  }

  def startFetchingSchedule(): Unit = {
    ResonatorApiClient
      .getRadioSchedule(GetRadioScheduleRequest(requestTimeMillis = js.Date.now().toLong))
      .foreach(setSchedule)

    if (fetchTimerId.isEmpty) {
      fetchTimerId = Some(dom.window.setTimeout(() => {
        fetchTimerId = None
        startFetchingSchedule()
      }, 60 * 1000))
    }
  }

  private def updateState(s: State): Unit = {
    if (s.on && state.off) {
      // TODO
    }

    if (s != state) {
      state = s
      stateObservable.notifyObservers(state)
    }
  }

  private def syncAudioPlayer() = {
    if (curState.on) {
      if (AudioPlayer.curState.status != PlayerStatuses.Playing) {
        playStatic()
      }

      val station = state.currentSchedules.find(_.getStation.frequency == curState.frequency)
      if (station.isDefined) {
        AudioPlayer.attemptPlayFromRadioSchedule(station.get)
      } else {
        AudioPlayer.off(OffSources.RadioControls)
      }
    } else {
      pauseStatic()
      AudioPlayer.off(OffSources.RadioPower)
    }
  }

  private def playStatic() = {
    if (!staticHowl.playing()) {
      staticHowl.setSeek(staticHowl.duration() * js.Math.random())
      staticHowl.play()
    }
  }

  private def pauseStatic() = {
    staticHowl.stop()
  }

  def randomOption[T](xs: Seq[T]): Option[T] = {
    if (xs.nonEmpty) {
      Some(xs(Random.nextInt(xs.length)))
    } else {
      None
    }
  }
}
