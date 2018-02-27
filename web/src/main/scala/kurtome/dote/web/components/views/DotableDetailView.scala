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
import kurtome.dote.web.components.widgets.detail.{EpisodeDetails, PodcastDetails}
import kurtome.dote.web.rpc.{DoteProtoServer, LocalCache}
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.GlobalLoadingManager
import scalacss.internal.mutable.StyleSheet
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object DotableDetailView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._
  }

  case class Props(route: DetailsRoute)

  case class State(response: GetDotableDetailsResponse = GetDotableDetailsResponse.defaultInstance,
                   requestedId: String = "",
                   requestInFlight: Boolean = false)

  class Backend(val bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def fetchDetails(p: Props): Callback = Callback {
      val s = bs.state.runNow()
      val id = p.route.id

      if (s.requestedId != id) {
        LocalCache
          .getObj(LocalCache.ObjectKinds.DotableDetails, id)
          .map(_.map(Dotable.parseFrom)) map { cachedDetails =>
          if (cachedDetails.isDefined) {
            // Use cached
            bs.modState(
                _.copy(
                  response = GetDotableDetailsResponse(responseStatus =
                                                         Some(ActionStatus(success = true)),
                                                       dotable = cachedDetails)))
              .runNow()
            fetchFromServer(p)
          } else {
            // Use shallow cached copy if available
            LocalCache
              .getObj(LocalCache.ObjectKinds.DotableShallow, id)
              .map(_.map(Dotable.parseFrom)) map { cachedShallow =>
              val s = bs.state.runNow()
              if (cachedShallow.isDefined) {
                bs.modState(
                    _.copy(requestInFlight = true,
                           requestedId = id,
                           response = GetDotableDetailsResponse(
                             responseStatus = Some(ActionStatus(success = true)),
                             cachedShallow)))
                  .runNow()
              }

              // Request from server regardless, to get latest
              fetchFromServer(p)
            }
          }
        }

      }
    }

    def fetchFromServer(p: Props): Unit = {
      val request = GetDotableDetailsRequest(p.route.id)
      val f = DoteProtoServer.getDotableDetails(request) flatMap { response =>
        bs.modState(_.copy(response = response, requestInFlight = false)).toFuture
      }
      GlobalLoadingManager.addLoadingFuture(f)
    }

    def render(p: Props, s: State): VdomElement = {
      val dotable = s.response.getDotable
      Fade(in = true, timeoutMs = 300)(
        Grid(container = true, spacing = 0)(
          Grid(item = true, xs = 12)(
            dotable.kind match {
              case Kind.PODCAST => PodcastDetails(PodcastDetails.Props(dotable))()
              case Kind.PODCAST_EPISODE =>
                EpisodeDetails(EpisodeDetails.Props(dotable))()
              case _ => {
                // Waiting for data
                GridContainer(justify = Grid.Justify.Center)(
                  GridItem()(CircularProgress(variant = CircularProgress.Variant.Indeterminate)())
                )
              }
            }
          )
        )
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
    component.withProps(Props(route))
  }
}
