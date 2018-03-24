package kurtome.dote.web.components.views

import kurtome.dote.proto.api.action.get_feed._
import kurtome.dote.proto.api.feed._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.HomeId
import kurtome.dote.web.rpc.{DoteProtoServer, LocalCache}
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.materialui.Button
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui.GridContainer
import kurtome.dote.web.components.materialui.GridItem
import kurtome.dote.web.components.widgets.Announcement
import kurtome.dote.web.components.widgets.FlatRoundedButton
import kurtome.dote.web.components.widgets.MainContentSection
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.components.widgets.feed.VerticalFeed
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.rpc.LocalCache.ObjectKinds
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object HomeView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val announcementWrapper = style(
      marginTop(24 px),
      marginBottom(24 px)
    )

    val loginAnnouncementPaper = style(
      width(100.%%),
      backgroundColor.white
    )

    val loginButton = style(
      backgroundColor :=! MuiTheme.theme.palette.primary.light
    )
  }

  case class Props()
  case class State(feed: Feed = Feed.defaultInstance, isFeedLoading: Boolean = true)

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
      LocalCache
        .getObj(ObjectKinds.Feed, FeedId().withHome(HomeId.defaultInstance).toString)
        .map(_.map(Feed.parseFrom)) map { cachedFeed =>
        if (cachedFeed.isDefined) {
          bs.modState(_.copy(feed = cachedFeed.get, isFeedLoading = false)).runNow()
        }

        // get the latest data as well, in case it has changed
        val f = DoteProtoServer.getFeed(GetFeedRequest(
          maxItems = 20,
          maxItemSize = 10,
          id = Some(FeedId().withHome(HomeId())))) map { response =>
          bs.modState(_.copy(feed = response.getFeed, isFeedLoading = false)).runNow()
        }
        GlobalLoadingManager.addLoadingFuture(f)
      }

    }

    def render(p: Props, s: State): VdomElement = {
      <.div(
        if (!LoggedInPersonManager.isLoggedIn) {
          MainContentSection(variant = MainContentSection.Variants.Primary)(
            GridContainer(justify = Grid.Justify.SpaceBetween,
                          alignItems = Grid.AlignItems.Center)(
              GridItem(xs = 12, md = 8)(
                <.div(
                  ^.color := "white",
                  Announcement()(
                    "Keep track of your favorite podcasts, " +
                      "and see what you friends are listening to."
                  )
                )
              ),
              GridItem()(
                FlatRoundedButton(variant = FlatRoundedButton.Variants.Fill,
                                  onClick = doteRouterCtl.set(LoginRoute))("Sign Up for Free")
              )
            )
          )
        } else {
          <.div()
        },
        VerticalFeed(s.feed, s.isFeedLoading)()
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
