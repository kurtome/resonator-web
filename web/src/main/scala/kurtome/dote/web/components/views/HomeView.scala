package kurtome.dote.web.components.views

import kurtome.dote.proto.api.action.get_feed._
import kurtome.dote.proto.api.feed._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.feed.FeedId.HomeId
import kurtome.dote.web.rpc.{DoteProtoServer, LocalCache}
import kurtome.dote.web.components.widgets.ContentFrame
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.widgets.feed.FeedDotableList
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.lib.LazyLoad
import kurtome.dote.web.rpc.LocalCache.ObjectKinds
import kurtome.dote.web.utils._
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

  case class Props(routerCtl: DoteRouterCtl, loginAttempted: Boolean = false)
  case class State(feed: Feed = Feed.defaultInstance)

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    val handleDidMount = Callback {
      fetchHomeData()

      if (LoggedInPersonManager.loginAttempted && !LoggedInPersonManager.displayedLoginSnack) {
        val person = LoggedInPersonManager.person
        LoggedInPersonManager.displayedLoginSnack = true
        if (person.isEmpty) {
          GlobalNotificationManager.displayMessage("Login link was expired, please login again.")
        } else {
          GlobalNotificationManager.displayMessage(s"Logged in as ${person.get.username}")
        }
      }

    }

    def fetchHomeData() = {
      val cachedFeed = LocalCache.getObj(ObjectKinds.Feed, "home", Feed.parseFrom)
      if (cachedFeed.isDefined) {
        bs.modState(_.copy(feed = cachedFeed.get)).runNow()
      }

      // get the latest data as well, in case it has changed
      val f = DoteProtoServer.getFeed(GetFeedRequest(
        maxItems = 20,
        maxItemSize = 10,
        id = Some(FeedId().withHomeId(HomeId())))) map { response =>
        bs.modState(_.copy(feed = response.getFeed)).runNow()
      }
      GlobalLoadingManager.addLoadingFuture(f)
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
                  LazyLoad(once = true, height = 200)(
                    FeedDotableList(p.routerCtl, item.getDotableList, key = Some(i.toString))()
                  )
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
