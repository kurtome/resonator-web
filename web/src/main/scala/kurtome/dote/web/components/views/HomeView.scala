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
import kurtome.dote.web.components.widgets.Announcement
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.rpc.LocalCache.ObjectKinds
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object HomeView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val feedItemContainer = style(
      marginBottom(24 px)
    )

    val announcementWrapper = style(
      marginTop(24 px),
      marginBottom(24 px)
    )
  }

  case class Props()
  case class State(feed: Feed = Feed.defaultInstance)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleDidMount = Callback {
      fetchHomeData()

      if (LoggedInPersonManager.loginAttempted && !LoggedInPersonManager.displayedLoginSnack) {
        val person = LoggedInPersonManager.person
        LoggedInPersonManager.displayedLoginSnack = true
        if (person.isEmpty) {
          GlobalNotificationManager.displayError("Login link was expired, please login again.")
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
        Grid(item = true, xs = 12)(
          Grid(container = true, spacing = 0, justify = Grid.Justify.Center)(
            Grid(item = true, xs = 12, style = Styles.announcementWrapper)(
              if (!LoggedInPersonManager.isLoggedIn) {
                Announcement()(
                  "Keep track of your favorite podcasts, and see what you friends are listening to. ",
                  SiteLink(LoginRoute)("Create an account to get started.")
                )
              } else {
                Announcement()(
                  "Share your ",
                  SiteLink(ProfileRoute(LoggedInPersonManager.person.get.username))(
                    "profile page"),
                  " to show off your favorite podcasts."
                )
              }
            )
          )
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
        Grid(item = true, xs = 12)(
          Grid(container = true, justify = Grid.Justify.Center)(
            Grid(item = true, xs = 12, style = Styles.announcementWrapper)(
              Announcement()(
                "Can't find what you're looking for? Try ",
                SiteLink(SearchRoute)("searching for a podcast"),
                "."
              )
            )
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
