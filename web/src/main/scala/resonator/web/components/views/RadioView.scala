package resonator.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.radio.RadioStation.FrequencyKind
import resonator.proto.api.radio.RadioStationSchedule
import resonator.shared.util.observer.Observer
import resonator.web.CssSettings._
import resonator.web.DoteRoutes._
import resonator.web.audio.RadioTuner
import resonator.web.components.ComponentHelpers
import resonator.web.components.materialui
import resonator.web.components.materialui.Grid
import resonator.web.components.materialui.GridContainer
import resonator.web.components.materialui.GridItem
import resonator.web.components.materialui.Hidden
import resonator.web.components.materialui.IconButton
import resonator.web.components.materialui.Icons
import resonator.web.components.materialui.Switch
import resonator.web.components.materialui.Typography
import resonator.web.components.widgets
import resonator.web.components.widgets.MainContentSection
import resonator.web.components.widgets.SiteLink
import resonator.web.components.widgets.card.EpisodeCard
import resonator.web.components.widgets.radio.TunerBand
import resonator.web.constants.MuiTheme
import resonator.web.utils.BaseBackend
import resonator.web.utils.UniversalAnalytics
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js

object RadioView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val summaryContainer = style(
      padding(8 px)
    )

    val episodeItem = style(
      width(50 %%),
      minWidth(300 px)
    )

    val powerButtonGlow = style(
      width(100 %%),
      height(100 %%),
      position.absolute,
      animation := s"${Animations.fadeInOut.name.value} 2s infinite",
      background := s"radial-gradient(${MuiTheme.theme.palette.secondary.light} 0%, transparent 50%)",
      borderRadius(50 %%)
    )

    val nowPlayingHeadline = style(
      marginBottom(16 px)
    )

  }

  private object Animations extends StyleSheet.Inline {
    import dsl._
    val fadeInOut = keyframes(
      (0 %%) -> keyframe(opacity(0)),
      (50 %%) -> keyframe(opacity(1)),
      (100 %%) -> keyframe(opacity(0))
    )
  }
  Animations.addToDocument()

  case class Props(initialRoute: RadioRoute)
  case class State(tunerState: RadioTuner.State)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    private val stationPattern = raw"([0-9]+.?[0-9]*)(kHz|MHz)".r

    def handleNewProps(p: Props) = Callback {
      p.initialRoute.station match {
        case stationPattern(frequencyStr, bandStr) => {
          bandStr match {
            case "kHz" => RadioTuner.setStation(FrequencyKind.AM, frequencyStr.toFloat)
            case "MHz" => RadioTuner.setStation(FrequencyKind.FM, frequencyStr.toFloat)
          }
        }
        case _ =>
      }
      RadioTuner.startFetchingSchedule()
    }

    def handleDidMount(p: Props) = Callback {}

    val stateObserver: Observer[RadioTuner.State] = (state: RadioTuner.State) => {
      bs.modState(_.copy(tunerState = state)).runNow()
    }

    RadioTuner.stateObservable.addObserver(stateObserver)

    val onUnmount: Callback = Callback {
      RadioTuner.stateObservable.removeObserver(stateObserver)
    }

    private def updateUrlToMatchStation(s: State) = {
      val state = s.tunerState
      if (state.currentStation.isDefined) {
        val stationStr = RadioTuner.formatFrequencyForRoute(state.currentStation.get)
        val title = {
          state.currentCallSign match {
            case Some(callSign) => s"$callSign - $stationStr | Resonator"
            case _ => s"$stationStr | Resonator"
          }
        }
        // put the current station in the navigation history, but replace the current state so
        // that it doesn't create a long stack of stations.
        replaceCurrentRoute(RadioRoute(stationStr))
      }
    }

    private def powerSwitched(e: ReactEventFromInput) = Callback {
      val checked = e.target.checked
      val onStr = if (checked) "on" else "off"
      UniversalAnalytics.event("radio-tuner", s"power-$onStr", dom.window.location.pathname)
      RadioTuner.power(checked)
    }

    private val seekForward = Callback {
      UniversalAnalytics.event("radio-tuner", "seek-forward", dom.window.location.pathname)
      RadioTuner.seekForward()
      updateUrlToMatchStation(bs.state.runNow())
    }

    private val seekBackward = Callback {
      UniversalAnalytics.event("radio-tuner", "seek-backward", dom.window.location.pathname)
      RadioTuner.seekBackward()
      updateUrlToMatchStation(bs.state.runNow())
    }

    def render(p: Props, s: State): VdomElement = {

      ReactFragment(
        MainContentSection(variant = MainContentSection.Variants.Light)(
          GridContainer()(
            GridItem(xs = 12)(
              GridContainer(spacing = 8,
                            direction = Grid.Direction.Column,
                            alignItems = Grid.AlignItems.Center)(
                GridItem()(
                  <.div(^.textAlign.center, ^.marginBottom := "-8px", Typography()("Power")),
                  <.div(
                    ^.position := "relative",
                    Hidden(xsUp = s.tunerState.on)(
                      <.div(
                        ^.className := Styles.powerButtonGlow
                      )
                    ),
                    Switch(checked = s.tunerState.on, onChange = powerSwitched)()
                  )
                )
              )
            ),
            GridItem(xs = 12)(
              TunerBand(minFrequency = 500,
                        maxFrequency = 1700,
                        majorTickInterval = if (ComponentHelpers.isBreakpointXs) 200 else 100,
                        currentFrequency = s.tunerState.frequency)()
            ),
            GridItem(xs = 12)(
              GridContainer(spacing = 8,
                            justify = Grid.Justify.Center,
                            alignItems = Grid.AlignItems.Center)(
                IconButton(color = IconButton.Colors.Primary, onClick = seekBackward)(
                  Icons.ChevronLeft()),
                Typography()("Seek"),
                IconButton(color = IconButton.Colors.Primary, onClick = seekForward)(
                  Icons.ChevronRight())
              )
            )
          )
        ),
        MainContentSection()(
          Typography(variant = Typography.Variants.Headline, style = Styles.nowPlayingHeadline)(
            "Now Playing"),
          GridContainer(spacing = 16)(
            s.tunerState.currentSchedules map { stationSchedule =>
              val station = stationSchedule.getStation
              val episode = RadioTuner.currentEpisodeForSchedule(stationSchedule)
              GridItem(key = Some(s"now-playing-${station.callSign}"), xs = 12, sm = 6, lg = 4)(
                Typography(variant = Typography.Variants.SubHeading)(
                  s"${station.callSign} ",
                  SiteLink(RadioRoute(RadioTuner.formatFrequencyForRoute(station)))(
                    RadioTuner.formatFrequency(station))
                ),
                Hidden(xsUp = episode.isEmpty)(
                  EpisodeCard(episode.map(_.getEpisode).getOrElse(Dotable.defaultInstance))()
                ),
                Hidden(xsUp = episode.isDefined)(
                  Typography()("Off Air")
                )
              )
            } toVdomArray
          )
        )
      )
    }

  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => State(RadioTuner.curState))
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .componentWillMount(x => x.backend.handleNewProps(x.props))
    .componentWillReceiveProps(x => x.backend.handleNewProps(x.nextProps))
    .componentWillUnmount(x => x.backend.onUnmount)
    .build

  def apply(route: RadioDefaultRoute) =
    component.withProps(Props(RadioRoute("")))

  def apply(route: RadioRoute) = {
    component.withProps(Props(route))
  }
}
