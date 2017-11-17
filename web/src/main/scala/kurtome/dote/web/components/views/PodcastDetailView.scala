package kurtome.dote.web.components.views

import dote.proto.api.action.get_dotable._
import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.DoteRoutes.{DoteRouterCtl, PodcastDetailRoute}
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.{ContentFrame, EntityDetails}
import kurtome.dote.web.rpc.{DoteProtoServer, LocalCache}

import scala.concurrent.ExecutionContext.Implicits.global

object PodcastDetailView {

  case class Props(routerCtl: DoteRouterCtl, route: PodcastDetailRoute)

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
        Fade(in = true, transitionDurationMs = 300)(
          Grid(container = true, spacing = 0)(
            Grid(item = true, xs = 12)(
              EntityDetails(EntityDetails.Props(p.routerCtl, dotable))()
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

  def apply(props: Props) = component.withProps(props)
}
