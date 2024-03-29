package resonator.web.audio

import resonator.proto.api.action.get_radio_schedule.GetRadioScheduleRequest
import resonator.proto.api.action.get_radio_schedule.GetRadioScheduleResponse
import resonator.proto.api.radio.RadioStation
import resonator.proto.api.radio.RadioStation.FrequencyKind
import resonator.proto.api.radio.RadioStationSchedule
import resonator.proto.api.radio.ScheduledEpisode
import resonator.shared.util.observer.Observable
import resonator.shared.util.observer.Observer
import resonator.shared.util.observer.SimpleObservable
import resonator.web.DoteRoutes.DoteRoute
import resonator.web.DoteRoutes.RadioDefaultRoute
import resonator.web.DoteRoutes.RadioRoute
import resonator.web.audio.AudioPlayer.OffSources
import resonator.web.audio.AudioPlayer.PlayerStatuses
import resonator.web.rpc.ResonatorApiClient
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

    def currentStationSchedule: Option[RadioStationSchedule] = {
      currentSchedules.find(_.getStation.frequency == frequency)
    }

    def currentStation: Option[RadioStation] = {
      currentSchedules.find(_.getStation.frequency == frequency).map(_.getStation)
    }

    def currentCallSign: Option[String] = currentStationSchedule.map(_.getStation.callSign)

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

  def curRoute: DoteRoute =
    curState.currentStation
      .map(formatFrequencyForRoute)
      .map(RadioRoute(_))
      .getOrElse(RadioDefaultRoute())

  private val audioPlayerStateObserver: Observer[AudioPlayer.State] =
    (audioPlayerState: AudioPlayer.State) => {
      if (audioPlayerState.status == PlayerStatuses.Off && audioPlayerState.offSource == OffSources.CloseButton) {
        power(false)
        pauseStatic()
      } else {
        if (curState.off || audioPlayerState.status == PlayerStatuses.Playing) {
          pauseStatic()
        } else {
          playStatic()
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
    syncAudioPlayer()
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
    if (fetchTimerId.isEmpty) {
      ResonatorApiClient
        .getRadioSchedule(GetRadioScheduleRequest(requestTimeMillis = js.Date.now().toLong))
        .foreach(setSchedule)

      fetchTimerId = Some(dom.window.setTimeout(() => {
        fetchTimerId = None
        startFetchingSchedule()
      }, 60 * 1000))
    }
  }

  private def updateState(s: State): Unit = {
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

  def currentEpisodeForSchedule(schedule: RadioStationSchedule): Option[ScheduledEpisode] = {
    schedule.scheduledEpisodes find { se =>
      val now = js.Date.now()
      se.startTimeMillis <= now && se.endTimeMillis >= now && AudioPlayer.canPlay(se.getEpisode)
    }
  }

  def formatStation(station: RadioStation): String = {
    s"${station.callSign} - ${formatFrequency(station)}"
  }

  def formatFrequency(station: RadioStation): String = {
    station.frequencyKind match {
      case RadioStation.FrequencyKind.AM => s"${station.frequency} kHz"
      case RadioStation.FrequencyKind.FM => s"${station.frequency} MHz"
      case _ => ""
    }
  }

  def formatFrequencyForRoute(station: RadioStation): String = {
    station.frequencyKind match {
      case RadioStation.FrequencyKind.AM => s"${station.frequency}kHz"
      case RadioStation.FrequencyKind.FM => s"${station.frequency}MHz"
      case _ => station.frequency.toString
    }
  }

}
