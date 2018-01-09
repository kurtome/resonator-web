package kurtome.dote.web.components.views

import dote.proto.api.action.get_feed_controller._
import dote.proto.api.feed.{Feed, FeedItem}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.rpc.{DoteProtoServer, LocalCache}
import kurtome.dote.web.components.widgets.ContentFrame
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.widgets.feed.FeedDotableList
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.rpc.LocalCache.ObjectKinds
import kurtome.dote.web.utils.GlobalLoadingManager
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object HomeView extends LogSupport {

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val feedItemContainer = style(
      marginTop(24 px)
    )

  }
  Styles.addToDocument()

  case class LoginData(email: String, code: String)
  case class Props(routerCtl: DoteRouterCtl)
  case class State(feed: Feed = Feed.defaultInstance, requestInFlight: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    val handleDidMount = Callback {
      fetchHomeData()
    }

    def fetchHomeData() = {
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

    def render(p: Props, s: State): VdomElement = {
      ContentFrame(p.routerCtl)(
        s.feed.items.zipWithIndex map {
          case (item, i) =>
            <.div(
              ^.key := i,
              ^.className := Styles.feedItemContainer,
              item.kind match {
                case FeedItem.Kind.DOTABLE_LIST =>
                  FeedDotableList(p.routerCtl, item.getDotableList, key = Some(i.toString))()
                case _ => <.div(^.key := i)
              }
            )
        } toVdomArray
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .componentDidMount(x => x.backend.handleDidMount)
    .build

  def apply(routerCtl: DoteRouterCtl) = component.withProps(Props(routerCtl))
}
