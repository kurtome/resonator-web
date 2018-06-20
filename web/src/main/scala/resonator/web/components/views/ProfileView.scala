package resonator.web.components.views

import resonator.proto.api.feed._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.action.get_feed.GetFeedRequest
import resonator.proto.api.feed.FeedId
import resonator.proto.api.feed.FeedId.ProfileId
import resonator.proto.api.person.Person
import resonator.web.CssSettings._
import resonator.web.DoteRoutes._
import resonator.web.SharedStyles
import resonator.web.components.materialui._
import resonator.web.components.widgets.Announcement
import resonator.web.components.widgets.MainContentSection
import resonator.web.components.widgets.SiteLink
import resonator.web.components.widgets.button.ShareButton
import resonator.web.components.widgets.feed.VerticalFeed
import resonator.web.rpc.ResonatorApiClient
import resonator.web.utils.GlobalLoadingManager
import resonator.web.utils.LoggedInPersonManager
import resonator.web.utils.BaseBackend
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object ProfileView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val fieldsContainer = style(
      marginLeft(SharedStyles.spacingUnit),
      marginRight(SharedStyles.spacingUnit)
    )

    val accountInfoContainer = style(
      padding(SharedStyles.spacingUnit),
      marginBottom(SharedStyles.spacingUnit * 2)
    )

    val accountInfoHeaderContainer = style(
      marginBottom(-SharedStyles.spacingUnit * 3)
    )

    val logoutButton = style(
      float.right
    )

    val profileHeader = style(
      display.inline
    )

    val announcementText = style(
      fontSize(1.5 rem)
    )
  }

  case class Props(username: String)
  case class State(feed: Feed = Feed.defaultInstance, isFeedLoading: Boolean = true)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleNewProps = (p: Props) =>
      Callback {
        fetchProfileFeed(p)
    }

    val handleLogout = Callback {
      dom.document.location.assign("/logout")
    }

    private def fetchProfileFeed(p: Props): Unit = {
      val f = ResonatorApiClient.getFeed(
        GetFeedRequest(maxItems = 20,
                       maxItemSize = 10,
                       id = Some(FeedId().withProfile(ProfileId(username = p.username))))) map {
        response =>
          bs.modState(_.copy(feed = response.getFeed, isFeedLoading = false)).runNow()
      }
      GlobalLoadingManager.addLoadingFuture(f)
    }

    private def isProfileForLoggedInPerson(p: Props) = {
      LoggedInPersonManager.isLoggedInPerson(p.username)
    }

    def renderAccountInfo(p: Props, s: State): VdomElement = {
      val loggedInPerson = if (LoggedInPersonManager.isLoggedIn) {
        LoggedInPersonManager.person.get
      } else {
        Person.defaultInstance
      }

      <.div(
        ^.className := Styles.accountInfoContainer,
        Grid(container = true,
             justify = Grid.Justify.FlexStart,
             alignItems = Grid.AlignItems.Center)(
          Grid(item = true, xs = 12, style = Styles.accountInfoHeaderContainer)(
            Grid(container = true,
                 justify = Grid.Justify.SpaceBetween,
                 alignItems = Grid.AlignItems.Baseline)(
              Grid(item = true)(
                Typography(variant = Typography.Variants.SubHeading)("Your info")
              ),
              Grid(item = true)(
                Button(onClick = handleLogout)("Logout")
              )
            )
          ),
          Grid(item = true)(
            TextField(
              autoFocus = false,
              disabled = true,
              value = loggedInPerson.username,
              name = "username",
              label = Typography()("username")
            )()),
          Grid(item = true)(
            TextField(
              autoFocus = false,
              disabled = true,
              value = loggedInPerson.email,
              inputType = "email",
              name = "email",
              label = Typography()("email address")
            )()
          ),
          Grid(item = true, xs = 12)(
            Typography(variant = Typography.Variants.Caption)(
              "Only you can see this section, it will not show up on your profile for others."))
        )
      )
    }

    def render(p: Props, s: State): VdomElement = {
      <.div(
        VerticalFeed(s.feed, s.isFeedLoading)(),
        Hidden(xsUp = !isProfileForLoggedInPerson(p))(
          MainContentSection(variant = MainContentSection.Variants.Light)(renderAccountInfo(p, s))
        )
      )

    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .componentDidMount(x => x.backend.handleNewProps(x.props))
    .componentWillReceiveProps(x => x.backend.handleNewProps(x.nextProps))
    .build

  def apply(route: ProfileRoute) =
    component.withProps(Props(route.username))
}
