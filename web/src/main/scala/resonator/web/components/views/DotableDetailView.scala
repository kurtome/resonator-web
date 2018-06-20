package resonator.web.components.views

import resonator.proto.api.action.get_dotable._
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.dotable.Dotable.Kind
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.common.ActionStatus
import resonator.web.DoteRoutes._
import resonator.web.CssSettings._
import resonator.web.components.materialui._
import resonator.web.components.widgets.MainContentSection
import resonator.web.components.widgets.detail.ReviewDetails
import resonator.web.components.widgets.detail.{EpisodeDetails, PodcastDetails}
import resonator.web.components.widgets.feed.FeedContainer
import resonator.web.components.widgets.feed.VerticalFeed
import resonator.web.rpc.CachedValue
import resonator.web.rpc.EmptyCachedValue
import resonator.web.rpc.ResonatorApiClient
import resonator.web.utils.BaseBackend
import resonator.web.utils.GlobalLoadingManager
import org.scalajs.dom
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

    def handleNewProps(p: Props): Callback = Callback {
      if (bs.state.runNow().requestedId != p.id) {
        // reset the state
        bs.setState(State()).runNow()

        fetchDetails(p)
      }
    }

    def fetchDetails(p: Props): Unit = {
      val s = bs.state.runNow()
      val id = p.id

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

    def fetchFromServer(p: Props): Unit = {
      val request = GetDotableDetailsRequest(p.id)
      val f = ResonatorApiClient.getDotableDetails(request) map { response =>
        if (response.getDotable.id == p.id) {
          bs.modState(_.copy(response = response, requestInFlight = false)).runNow()
        }
      }
      GlobalLoadingManager.addLoadingFuture(f)
    }

    def render(p: Props, s: State): VdomElement = {
      val dotable = s.response.getDotable

      val title = dotable.getCommon.title
      if (title.nonEmpty) {
        dom.document.title = s"$title | Resonator"
      }

      val episodeTablePage = Try(p.queryParams.getOrElse("ep_page", "0").toInt).getOrElse(0)

      ReactFragment(
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
        ),
        FeedContainer(s.response.getFeedId)()
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

  def apply(route: DetailsRoute) = {
    component.withKey(s"details-${route.id}").withProps(Props(route.id, route.queryParams))
  }
}
