package kurtome.dote.web.components.views

import dote.proto.api.action.get_feed_controller.{GetFeedRequest, GetFeedResponse}
import dote.proto.api.feed.FeedItem
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.components.widgets.{ContentFrame}
import kurtome.dote.web.DoteRoutes.{DoteRoute, DoteRouterCtl}
import kurtome.dote.web.components.widgets.feed.FeedDotableList
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.ComponentHelpers._

import scala.concurrent.ExecutionContext.Implicits.global

object HomeView {

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val feedItemContainer = style(
      marginTop(24 px)
    )

  }
  Styles.addToDocument()

  case class State(request: GetFeedRequest = GetFeedRequest.defaultInstance,
                   response: GetFeedResponse = GetFeedResponse.defaultInstance,
                   requestInFlight: Boolean = false)

  class Backend(bs: BackendScope[DoteRouterCtl, State]) {

    def fetchData(): Callback = Callback {
      bs.modState(_.copy(requestInFlight = true)).runNow
      DoteProtoServer.getFeed(GetFeedRequest(maxItems = 20, maxItemSize = 10)) map { response =>
        bs.modState(_.copy(response = response, requestInFlight = false)).runNow()
      }
    }

    def render(routerCtl: DoteRouterCtl, s: State): VdomElement = {
      ContentFrame(ContentFrame.Props(routerCtl))(
        s.response.getFeed.items.zipWithIndex map {
          case (item, i) =>
            <.div(
              ^.key := i,
              ^.className := Styles.feedItemContainer,
              item.kind match {
                case FeedItem.Kind.DOTABLE_LIST =>
                  FeedDotableList(routerCtl, item.getDotableList, key = Some(i.toString))()
                case _ => <.div(^.key := i)
              }
            )
        } toVdomArray
      )
    }
  }

  val component = ScalaComponent
    .builder[DoteRouterCtl](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .componentDidMount(_.backend.fetchData())
    .build

  def apply(routerCtl: RouterCtl[DoteRoute]) = component.withProps(routerCtl)
}
