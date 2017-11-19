package kurtome.dote.web.components.views

import dote.proto.api.action.get_dotable._
import dote.proto.api.dotable.Dotable
import dote.proto.api.dotable.Dotable.Kind
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.{DoteRouterCtl, DotableRoute}
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.ContentFrame
import kurtome.dote.web.components.widgets.detail.{EpisodeDetails, PodcastDetails}
import kurtome.dote.web.rpc.{DoteProtoServer, LocalCache}

import scala.concurrent.ExecutionContext.Implicits.global

object DotableDetailView {

  case class Props(routerCtl: DoteRouterCtl, route: DotableRoute)

  case class State(request: GetDotableDetailsRequest,
                   response: GetDotableDetailsResponse,
                   requestInFlight: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) {

    def fetchDetails(s: State): Callback = {
      val id = s.request.id
      val cachedDetails: Option[Dotable] = LocalCache.get(includesDetails = true, id)

      if (cachedDetails.isDefined) {
        // Use cached
        bs.modState(
          _.copy(response = GetDotableDetailsResponse(cachedDetails), requestInFlight = false))
      } else {
        // Request from server, and use shallow cached data if available
        val cachedShallow: Option[Dotable] = LocalCache.get(includesDetails = false, id)
        bs.modState(_.copy(requestInFlight = true,
                           response = GetDotableDetailsResponse(cachedShallow))) flatMap { _ =>
          CallbackTo(DoteProtoServer.addGetDotableDetails(s.request) flatMap { response =>
            bs.modState(_.copy(response = response, requestInFlight = false)).toFuture
          })
        }
      }
    }

    def render(p: Props, s: State): VdomElement = {
      val dotable = s.response.getDotable
      ContentFrame(ContentFrame.Props(p.routerCtl))(
        Fade(in = true, timeoutMs = 300)(
          Grid(container = true, spacing = 0)(
            Grid(item = true, xs = 12)(
              dotable.kind match {
                case Kind.PODCAST => PodcastDetails(PodcastDetails.Props(p.routerCtl, dotable))()
                case Kind.PODCAST_EPISODE =>
                  EpisodeDetails(EpisodeDetails.Props(p.routerCtl, dotable))()
                case _ => <.div()
              }
            )
          ))
      )
    }
  }

  val component = ScalaComponent
    .builder[Props]("PodcastDetailView")
    .initialStateFromProps(props =>
      State(GetDotableDetailsRequest(props.route.id), GetDotableDetailsResponse()))
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .componentDidMount(x => x.backend.fetchDetails(x.state))
    .build

  def apply(routerCtl: DoteRouterCtl, route: DotableRoute) =
    component.withProps(Props(routerCtl, route))
}
