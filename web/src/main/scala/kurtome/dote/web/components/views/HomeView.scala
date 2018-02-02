package kurtome.dote.web.components.views

import kurtome.dote.proto.api.action.get_feed._
import kurtome.dote.proto.api.feed._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.feed.FeedId.HomeId
import kurtome.dote.web.rpc.{DoteProtoServer, LocalCache}
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.widgets.feed.FeedDotableList
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.lib.LazyLoad
import kurtome.dote.web.components.materialui.Typography
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.rpc.LocalCache.ObjectKinds
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object HomeView extends LogSupport {

  private object Styles extends StyleSheet.Inline with MuiInlineStyleSheet {
    import dsl._

    val feedItemContainer = style(
      marginBottom(24 px)
    )

    val announcementText = style(
      fontSize(1.5 rem)
    )

  }
  Styles.addToDocument()
  import Styles._

  case class Props()
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
      Grid(container = true, spacing = 0, justify = Grid.Justify.Center)(
        Grid(item = true, xs = 12, sm = 10, md = 8)(
          if (!LoggedInPersonManager.isLoggedIn) {
            Typography(typographyType = Typography.Type.Body1,
                       style = Styles.announcementText.inline)(
              "Keep track of your favorite podcasts, and see what you friends are listening to. ",
              doteRouterCtl.link(LoginRoute)(^.className := SharedStyles.siteLink,
                                             "Login to get started.")
            )
          } else {
            Typography(typographyType = Typography.Type.Body1,
                       style = Styles.announcementText.inline)(
              "Share your ",
              doteRouterCtl.link(ProfileRoute(LoggedInPersonManager.person.get.username))(
                ^.className := SharedStyles.siteLink,
                "profile page"),
              " to show off your favorite podcasts."
            )
          }
        ),
        Grid(item = true, xs = 12)(
          s.feed.items.zipWithIndex map {
            case (item, i) =>
              <.div(
                ^.key := s"$i${item.getDotableList.getList.title}",
                ^.className := Styles.feedItemContainer,
                item.kind match {
                  case FeedItem.Kind.DOTABLE_LIST =>
                    LazyLoad(once = true, height = 200)(
                      FeedDotableList(item.getDotableList, key = Some(i.toString))()
                    )
                  case _ => <.div(^.key := i)
                }
              )
          } toVdomArray
        ),
        Grid(item = true, xs = 12, sm = 10, md = 8)(
          Typography(typographyType = Typography.Type.Body1,
                     style = Styles.announcementText.inline)(
            "Can't find what you're looking for? Try ",
            doteRouterCtl.link(SearchRoute)(^.className := SharedStyles.siteLink,
                                            "searching for a podcast"),
            "."
          )
        )
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

  def apply() = component.withProps(Props())
}
