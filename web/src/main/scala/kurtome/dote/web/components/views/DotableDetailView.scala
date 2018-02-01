package kurtome.dote.web.components.views

import kurtome.dote.proto.api.action.get_dotable._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable.Dotable.Kind
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.{ContentFrame, NavBar}
import kurtome.dote.web.components.widgets.detail.{EpisodeDetails, PodcastDetails}
import kurtome.dote.web.rpc.{DoteProtoServer, LocalCache}
import kurtome.dote.web.utils.GlobalLoadingManager

import scala.concurrent.ExecutionContext.Implicits.global

object DotableDetailView {

  case class Props(route: DetailsRoute)

  case class State(response: GetDotableDetailsResponse = GetDotableDetailsResponse.defaultInstance,
                   requestInFlight: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) {

    def fetchDetails(p: Props): Callback = {
      val id = p.route.id
      val request = GetDotableDetailsRequest(id)
      val cachedDetails: Option[Dotable] = LocalCache.get(includesDetails = true, id)

      if (cachedDetails.isDefined) {
        // Use cached
        bs.modState(_.copy(response = GetDotableDetailsResponse(cachedDetails)))
      }

      // Request from server regardless, to get latest
      val cachedShallow: Option[Dotable] = LocalCache.get(includesDetails = false, id)
      bs.modState(_.copy(requestInFlight = true,
                         response = GetDotableDetailsResponse(cachedShallow))) flatMap { _ =>
        Callback {
          val f = DoteProtoServer.getDotableDetails(request) flatMap { response =>
            bs.modState(_.copy(response = response, requestInFlight = false)).toFuture
          }
          GlobalLoadingManager.addLoadingFuture(f)
        }
      }
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
              case _ => <.div()
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
    .componentWillMount((x) => x.backend.fetchDetails(x.props))
    .build

  def apply(route: DetailsRoute) = {
    component.withProps(Props(route))
  }
}
