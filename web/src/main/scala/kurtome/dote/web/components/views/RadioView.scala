package kurtome.dote.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.radio.RadioStation.FrequencyKind
import kurtome.dote.shared.util.observer.Observer
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.audio.RadioTuner
import kurtome.dote.web.components.ComponentHelpers
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui.GridContainer
import kurtome.dote.web.components.materialui.GridItem
import kurtome.dote.web.components.materialui.IconButton
import kurtome.dote.web.components.materialui.Icons
import kurtome.dote.web.components.materialui.Switch
import kurtome.dote.web.components.materialui.Typography
import kurtome.dote.web.components.widgets.MainContentSection
import kurtome.dote.web.components.widgets.radio.TunerBand
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.UniversalAnalytics
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
  }

  case class Props(routeStation: String)
  case class State(tunerState: RadioTuner.State)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    private val stationPattern = raw"([0-9]+.?[0-9]*)(kHz|MHz)".r

    def handleNewProps(p: Props) = Callback {
      p.routeStation match {
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

    val stateObserver: Observer[RadioTuner.State] = (state: RadioTuner.State) => {
      bs.modState(_.copy(tunerState = state)).runNow()

      val desiredRoute = if (state.frequency > 0) {
        val stationStr = state.frequency.toString + (state.frequencyKind match {
          case FrequencyKind.FM => "MHz"
          case _ => "kHz"
        })
        RadioRoute(stationStr)
      } else {
        RadioDefaultRoute()
      }
      val url = doteRouterCtl.urlFor(desiredRoute).value
      val baseTunerUrl = doteRouterCtl.urlFor(RadioDefaultRoute()).value
      if (dom.window.location.href != url) {
        if (dom.window.location.href == baseTunerUrl) {
          dom.window.history.replaceState(new js.Object(), s"Resonator", url)
        } else {
          dom.window.history.pushState(new js.Object(), s"Resonator", url)
        }
      }
    }
    RadioTuner.stateObservable.addObserver(stateObserver)
    val onUnmount: Callback = Callback {
      RadioTuner.stateObservable.removeObserver(stateObserver)
    }

    private def powerSwitched(e: ReactEventFromInput) = Callback {
      val checked = e.target.checked
      UniversalAnalytics.visitor.event("radio-tuner",
                                       s"power-$checked",
                                       dom.window.location.pathname)
      RadioTuner.power(checked)
    }

    private val seekForward = Callback {
      UniversalAnalytics.visitor.event("radio-tuner", "seek-forward", dom.window.location.pathname)
      RadioTuner.seekForward()
    }

    private val seekBackward = Callback {
      UniversalAnalytics.visitor.event("radio-tuner",
                                       "seek-backward",
                                       dom.window.location.pathname)
      RadioTuner.seekForward()
    }

    def render(p: Props, s: State): VdomElement = {
      MainContentSection()(
        GridContainer()(
          GridItem(xs = 12)(
            GridContainer(spacing = 8,
                          direction = Grid.Direction.Column,
                          alignItems = Grid.AlignItems.Center)(
              GridItem()(
                <.div(^.textAlign.center, ^.marginBottom := "-8px", Typography()("Power")),
                Switch(checked = s.tunerState.on, onChange = powerSwitched)()
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
      )
    }

  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State(RadioTuner.curState))
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .componentWillMount(x => x.backend.handleNewProps(x.props))
    .componentWillReceiveProps(x => x.backend.handleNewProps(x.nextProps))
    .build

  def apply(route: RadioDefaultRoute) =
    component.withProps(Props(""))

  def apply(route: RadioRoute) = {
    component.withProps(Props(route.station))
  }
}
