package kurtome.dote.web.components.views

import dote.proto.api.action.get_dotable_list._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.Styles
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.EntityTile
import kurtome.dote.web.DoteRoutes.{DoteRoute, DoteRouterCtl, PodcastDetailRoute}
import kurtome.dote.web.rpc.LocalCache.LsCache

import scala.concurrent.ExecutionContext.Implicits.global

object HomeView {

  case class State(request: GetDotableListRequest,
                   response: GetDotableListResponse,
                   requestInFlight: Boolean = false)

  class Backend(bs: BackendScope[DoteRouterCtl, State]) {
    def fetchData(): Callback = Callback {
      LsCache.flush()
      bs.modState(_.copy(requestInFlight = true)).runNow
      DoteProtoServer.addGetDotableList(GetDotableListRequest(maxResults = 20)) map { response =>
        bs.modState(_.copy(response = response, requestInFlight = false)).runNow()
      }
    }

    def render(routerCtl: DoteRouterCtl, s: State): VdomElement = {
      <.div(
        Grid(container = true, justify = Grid.Justify.Center)(
          Grid(item = true, md = 12, lg = 8)(
            Grid(container = true, justify = Grid.Justify.Center, spacing = 24)(
              Grid(item = true, xs = 12)(
                Typography(className = Styles.titleText,
                           typographyType = Typography.Type.Display1)("Pod Feels")
              ),
              Grid(item = true, xs = 12)(
                Fade(in = true, transitionDurationMs = 1000)(
                  Grid(container = true,
                       spacing = 24,
                       alignItems = Grid.AlignItems.FlexStart,
                       justify = Grid.Justify.Center)(
                    s.response.dotables map { dotable =>
                      Grid(item = true)(
                        <.div(^.className := Styles.tileContainer.className.value,
                              EntityTile.component(EntityTile.Props(routerCtl, dotable = dotable)))
                      )
                    } toVdomArray
                  )
                )
              )
            )
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[DoteRouterCtl]("HomeView")
    .initialState(State(GetDotableListRequest(), GetDotableListResponse()))
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .componentDidMount(_.backend.fetchData())
    .build

  def apply(routerCtl: RouterCtl[DoteRoute]) = component(routerCtl)
}