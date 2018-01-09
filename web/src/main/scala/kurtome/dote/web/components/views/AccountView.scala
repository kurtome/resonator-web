package kurtome.dote.web.components.views

import dote.proto.api.action.get_feed_controller.GetFeedRequest
import dote.proto.api.feed.Feed
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes.{DoteRoute, DoteRouterCtl}
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.ContentFrame
import kurtome.dote.web.rpc.LocalCache.ObjectKinds
import kurtome.dote.web.rpc.{DoteProtoServer, LocalCache}
import kurtome.dote.web.utils.GlobalLoadingManager
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object AccountView extends LogSupport {

  private object Styles extends StyleSheet.Inline {
    import dsl._

  }
  Styles.addToDocument()

  case class State(feed: Feed = Feed.defaultInstance, requestInFlight: Boolean = false)

  class Backend(bs: BackendScope[DoteRouterCtl, State]) extends LogSupport {

    def fetchData(): Callback = Callback {
      val cachedFeed = LocalCache.getObj(ObjectKinds.Feed, "home", Feed.parseFrom)
      if (cachedFeed.isDefined) {
        bs.modState(_.copy(feed = cachedFeed.get, requestInFlight = false)).runNow()
      } else {
        bs.modState(_.copy(requestInFlight = true)).runNow
        val f = DoteProtoServer.getFeed(GetFeedRequest(maxItems = 20, maxItemSize = 10)) map {
          response =>
            bs.modState(_.copy(feed = response.getFeed, requestInFlight = false)).runNow()
        }
        GlobalLoadingManager.addLoadingFuture(f)
      }
    }

    def render(routerCtl: DoteRouterCtl, s: State): VdomElement = {
      ContentFrame(routerCtl)(
        Grid(container = true, justify = Grid.Justify.Center)(
          Grid(item = true, xs = 12, md = 8)(
            Paper()(
              Grid(container = true)(
                Grid(item = true, xs = 12)(
                  TextField()()
                )
              )
            )
          )
        )
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
