package resonator.web.components.views

import resonator.proto.api.action.get_feed._
import resonator.proto.api.feed._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.feed.FeedId
import resonator.proto.api.feed.FeedId.HomeId
import resonator.web.rpc.ResonatorApiClient
import resonator.web.DoteRoutes._
import resonator.web.CssSettings._
import resonator.web.components.materialui.Grid
import resonator.web.components.materialui.GridContainer
import resonator.web.components.materialui.GridItem
import resonator.web.components.materialui.Hidden
import resonator.web.components.widgets.Announcement
import resonator.web.components.widgets.FlatRoundedButton
import resonator.web.components.widgets.MainContentSection
import resonator.web.components.widgets.feed.VerticalFeed
import resonator.web.constants.MuiTheme
import resonator.web.rpc.CachedValue
import resonator.web.rpc.EmptyCachedValue
import resonator.web.rpc.TimeCachedValue
import resonator.web.utils._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Date

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

  var cachedHomeFeed: CachedValue[Feed] = EmptyCachedValue

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
      val cachedFeed = cachedHomeFeed.get
      if (cachedFeed.isDefined) {
        bs.modState(_.copy(feed = cachedFeed.get, isFeedLoading = false)).runNow()
      } else {
        val f = ResonatorApiClient.getFeed(GetFeedRequest(
          maxItems = 20,
          maxItemSize = 20,
          id = Some(FeedId().withHome(HomeId())))) map { response =>
          cachedHomeFeed = TimeCachedValue(Date.now() + 1000 * 60 * 2, response.getFeed)
          bs.modState(_.copy(feed = response.getFeed, isFeedLoading = false)).runNow()
        }
        GlobalLoadingManager.addLoadingFuture(f)
      }
    }

    def render(p: Props, s: State): VdomNode = {
      ReactFragment(
        Hidden(xsUp = LoggedInPersonManager.isLoggedIn)(
          MainContentSection(variant = MainContentSection.Variants.Primary)(
            GridContainer(justify = Grid.Justify.SpaceBetween,
                          alignItems = Grid.AlignItems.Center)(
              GridItem(xs = 12, md = 8)(
                <.div(
                  ^.color := "white",
                  Announcement()(
                    "Keep track of your favorite podcasts, " +
                      "and see what your friends are listening to."
                  )
                )
              ),
              GridItem()(
                FlatRoundedButton(variant = FlatRoundedButton.Variants.FillPrimary,
                                  onClick = doteRouterCtl.set(LoginRoute()))("Sign Up for Free")
              )
            )
          )
        ),
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
