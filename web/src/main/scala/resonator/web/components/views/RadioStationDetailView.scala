package resonator.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.action.get_radio_station_details._
import resonator.proto.api.action.update_radio_station_details._
import resonator.web.CssSettings._
import resonator.web.DoteRoutes._
import resonator.web.audio.RadioTuner
import resonator.web.components.materialui._
import resonator.web.components.widgets.MainContentSection
import resonator.web.components.widgets.SiteLink
import resonator.web.components.widgets.card.PodcastCard
import resonator.web.rpc.ResonatorApiClient
import resonator.web.utils.BaseBackend
import resonator.web.utils.Debounce
import resonator.web.utils.GlobalLoadingManager
import resonator.web.utils.LoggedInPersonManager
import scalacss.internal.mutable.StyleSheet
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object RadioStationDetailView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val addRemoveContainer = style(
      maxWidth(400 px),
      padding(8 px)
    )
  }

  case class Props(callSign: String, queryParams: Map[String, String])

  case class State(response: GetRadioStationDetailsResponse =
                     GetRadioStationDetailsResponse.defaultInstance,
                   requestedCallSign: String = "",
                   requestInFlight: Boolean = false,
                   inputPodcastId: String = "")

  class Backend(val bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def handleNewProps(p: Props): Callback = Callback {
      if (bs.state.runNow().requestedCallSign != p.callSign) {
        // reset the state
        bs.setState(State()).runNow()

        fetchDetails(p)
      }
    }

    def fetchDetails(p: Props): Unit = {

      val request = GetRadioStationDetailsRequest(p.callSign)

      bs.modState(_.copy(requestedCallSign = p.callSign, requestInFlight = true)).runNow()
      val f = ResonatorApiClient.getRadioStationDetails(request) map { response =>
        if (response.getStationDetails.getStation.callSign == p.callSign) {
          bs.modState(_.copy(response = response, requestInFlight = false)).runNow()
        }
      }
      GlobalLoadingManager.addLoadingFuture(f)
    }

    def render(p: Props, s: State): VdomElement = {
      val details = s.response.getStationDetails
      val station = details.getStation

      val episodeTablePage = Try(p.queryParams.getOrElse("ep_page", "0").toInt).getOrElse(0)

      def handleInputChanged(event: ReactEventFromInput): Callback = {
        event.persist()
        if (event.target != null) {
          bs.modState(_.copy(inputPodcastId = event.target.value))
        } else {
          Callback.empty
        }
      }

      val addPodcast = Debounce.debounce1(200, true) { (podcastId: String) =>
        debug(s"adding $podcastId")

        val request = UpdateRadioStationDetailsRequest(callSign = p.callSign)
          .withAddPodcastId(podcastId)

        bs.modState(_.copy(requestedCallSign = p.callSign, requestInFlight = true))
        ResonatorApiClient.updateRadioStation(request) map { response =>
          if (response.getStationDetails.getStation.callSign == p.callSign) {
            bs.modState(
                _.copy(response =
                         GetRadioStationDetailsResponse(responseStatus = response.responseStatus,
                                                        stationDetails = response.stationDetails),
                       requestInFlight = false))
              .runNow()
          }
        }
      }

      val removePodcast = Debounce.debounce1(200, true) { (podcastId: String) =>
        debug(s"removing $podcastId")

        val request = UpdateRadioStationDetailsRequest(callSign = p.callSign)
          .withRemovePodcastId(podcastId)

        bs.modState(_.copy(requestedCallSign = p.callSign, requestInFlight = true))
        ResonatorApiClient.updateRadioStation(request) map { response =>
          if (response.getStationDetails.getStation.callSign == p.callSign) {
            bs.modState(
                _.copy(response =
                         GetRadioStationDetailsResponse(responseStatus = response.responseStatus,
                                                        stationDetails = response.stationDetails),
                       requestInFlight = false))
              .runNow()
          }
        }
      }

      MainContentSection()(
        Hidden(xsUp = !s.requestInFlight)(CircularProgress()()),
        Hidden(xsUp = !(!s.requestInFlight && details.station.isEmpty))(
          s"Station ${p.callSign} not found."),
        Typography(variant = Typography.Variants.Headline)(station.callSign),
        Typography(variant = Typography.Variants.SubHeading)(
          SiteLink(RadioRoute(RadioTuner.formatFrequencyForRoute(station)))(
            RadioTuner.formatFrequency(station))),
        GridContainer(spacing = 16)(
          Hidden(xsUp = s.requestInFlight || !LoggedInPersonManager.isAdmin)(
            GridItem(xs = 12)(
              Paper(elevation = 0, style = Styles.addRemoveContainer)(
                TextField(
                  autoFocus = false,
                  fullWidth = true,
                  placeholder = "podcast ID",
                  value = s.inputPodcastId,
                  onChange = handleInputChanged
                )(),
                Button(Button.Colors.Primary, onClick = Callback(addPodcast(s.inputPodcastId)))(
                  "Add"),
                Button(Button.Colors.Secondary,
                       onClick = Callback(removePodcast(s.inputPodcastId)))("Remove")
              )
            )
          ),
          details.podcasts map { podcast =>
            GridItem(xs = 12, md = 6, key = Some(s"station-podcast-item-${podcast.id}"))(
              PodcastCard(podcast)()
            )
          } toVdomArray
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((b, p, s) => b.backend.render(p, s))
    .componentWillReceiveProps((x) => x.backend.handleNewProps(x.nextProps))
    .componentDidMount((x) => x.backend.handleNewProps(x.props))
    .build

  def apply(route: RadioStationDetailRoute) = {
    component
      .withKey(s"station-details-${route.callSign}")
      .withProps(Props(route.callSign, route.queryParams))
  }
}
