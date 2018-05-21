package kurtome.dote.web.components.views

import kurtome.dote.proto.api.action.get_dotable._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable.Dotable.Kind
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.common.ActionStatus
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.MainContentSection
import kurtome.dote.web.components.widgets.detail.ReviewDetails
import kurtome.dote.web.components.widgets.detail.{EpisodeDetails, PodcastDetails}
import kurtome.dote.web.rpc.CachedValue
import kurtome.dote.web.rpc.EmptyCachedValue
import kurtome.dote.web.rpc.ResonatorApiClient
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.GlobalLoadingManager
import scalacss.internal.mutable.StyleSheet
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object DotableDetailView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._
  }

  var cachedDotable: CachedValue[Dotable] = EmptyCachedValue

  case class Props(id: String, queryParams: Map[String, String])

  case class State(response: GetDotableDetailsResponse = GetDotableDetailsResponse.defaultInstance,
                   requestedId: String = "",
                   requestInFlight: Boolean = false)

  class Backend(val bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def fetchDetails(p: Props): Callback = Callback {
      val s = bs.state.runNow()
      val id = p.id

      if (s.requestedId != id) {
        val cached = cachedDotable.get
        if (cached.isDefined && cached.get.id == id) {
          bs.modState(
              _.copy(requestInFlight = true,
                     requestedId = id,
                     response = GetDotableDetailsResponse(responseStatus =
                                                            Some(ActionStatus(success = true)),
                                                          cached)))
            .runNow()
        }

        // Request from server regardless, to get latest
        fetchFromServer(p)
      }
    }

    def fetchFromServer(p: Props): Unit = {
      val request = GetDotableDetailsRequest(p.id)
      val f = ResonatorApiClient.getDotableDetails(request) map { response =>
        bs.modState(_.copy(response = response, requestInFlight = false)).runNow()
      }
      GlobalLoadingManager.addLoadingFuture(f)
    }

    def render(p: Props, s: State): VdomElement = {
      val dotable = s.response.getDotable

      val episodeTablePage = Try(p.queryParams.getOrElse("ep_page", "0").toInt).getOrElse(0)

      MainContentSection()(
        dotable.kind match {
          case Kind.PODCAST =>
            PodcastDetails(PodcastDetails.Props(dotable, episodeTablePage))()
          case Kind.PODCAST_EPISODE => EpisodeDetails(EpisodeDetails.Props(dotable))()
          case Kind.REVIEW => ReviewDetails(ReviewDetails.Props(dotable))()
          case _ => {
            // Waiting for data
            GridContainer(justify = Grid.Justify.Center)(
              GridItem()(CircularProgress(variant = CircularProgress.Variant.Indeterminate)())
            )
          }
        }
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((b, p, s) => b.backend.render(p, s))
    .componentWillReceiveProps((x) => x.backend.fetchDetails(x.nextProps))
    .componentDidMount((x) => x.backend.fetchDetails(x.props))
    .build

  def apply(route: DetailsRoute) = {
    component.withProps(Props(route.id, route.queryParams))
  }
}
