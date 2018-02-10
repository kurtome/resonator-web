package kurtome.dote.web.components.views

import kurtome.dote.proto.api.feed.Feed
import kurtome.dote.proto.api.feed.FeedItem
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.get_feed.GetFeedRequest
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.ProfileId
import kurtome.dote.proto.api.person.Person
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.lib.LazyLoad
import kurtome.dote.web.components.widgets.Announcement
import kurtome.dote.web.components.widgets.ContentFrame
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.components.widgets.button.ShareButton
import kurtome.dote.web.components.widgets.feed.FeedDotableList
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.CopyToClipboard
import kurtome.dote.web.utils.GlobalLoadingManager
import kurtome.dote.web.utils.GlobalNotificationManager
import kurtome.dote.web.utils.GlobalNotificationManager.Notification
import kurtome.dote.web.utils.LoggedInPersonManager
import kurtome.dote.web.utils.BaseBackend
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

    val feedItemContainer = style(
      marginBottom(SharedStyles.spacingUnit * 3)
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
  case class State(feed: Feed = Feed.defaultInstance, following: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleNewProps = (p: Props) =>
      Callback {
        val f = DoteProtoServer.getFeed(
          GetFeedRequest(
            maxItems = 20,
            maxItemSize = 10,
            id = Some(FeedId().withProfileId(ProfileId(username = p.username))))) map { response =>
          bs.modState(_.copy(feed = response.getFeed)).runNow()
        }
        GlobalLoadingManager.addLoadingFuture(f)
    }

    val handleLogout = Callback {
      dom.document.location.assign("/logout")
    }

    def renderAccountInfo(p: Props, s: State): VdomElement = {
      val loggedInPerson = if (LoggedInPersonManager.isLoggedIn) {
        LoggedInPersonManager.person.get
      } else {
        Person.defaultInstance
      }

      if (isProfileForLoggedInPerson(p)) {
        Paper(elevation = 1, style = Styles.accountInfoContainer)(
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
      } else {
        <.div()
      }
    }

    private def isProfileForLoggedInPerson(p: Props) = {
      LoggedInPersonManager.isLoggedIn && p.username == LoggedInPersonManager.person.get.username
    }

    private def handleFollowingChanged(p: Props, s: State)(event: ReactEventFromInput) = Callback {
      val checked = event.target.checked
      bs.modState(_.copy(following = checked)).runNow()
    }

    def render(p: Props, s: State): VdomElement = {

      Grid(container = true, justify = Grid.Justify.Center)(
        Grid(item = true, xs = 12)(
          renderAccountInfo(p, s)
        ),
        Grid(item = true, xs = 12)(
          Grid(container = true, justify = Grid.Justify.FlexStart, spacing = 8)(
            Grid(item = true, xs = 12)(
              Typography(variant = Typography.Variants.Headline, style = Styles.profileHeader)(
                s"${p.username}'s profile"),
              <.br(),
              FormControlLabel(control = Checkbox(checked = s.following,
                                                  name = "follow",
                                                  value = "follow",
                                                  onChange = handleFollowingChanged(p, s))(),
                               label = Typography()(s"Follow ${p.username}"))()
            ),
            Grid(item = true, xs = 12)(
              ),
            Grid(item = true, xs = 12)(
              Announcement(size = Announcement.Sizes.Sm)(
                "Profile pages are shareable, text it to a friend or share online.")
            ),
            Grid(item = true, xs = 12)(
              ShareButton()()
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
        GridItem(xs = 12)(
          GridContainer(justify = Grid.Justify.Center)(
            GridItem(xs = 12)(
              if (isProfileForLoggedInPerson(p)) {
                Announcement(size = Announcement.Sizes.Sm)(
                  "Your recent activity will show up on your profile. Find something new from the ",
                  SiteLink(HomeRoute)("popular podcasts"),
                  " or try ",
                  SiteLink(SearchRoute)("searching for a podcast"),
                  " you already love."
                )
              } else if (LoggedInPersonManager.isLoggedIn) {
                Announcement(size = Announcement.Sizes.Sm)(
                  "Checkout your own ",
                  SiteLink(ProfileRoute(LoggedInPersonManager.person.get.username))(
                    "profile page"),
                  "."
                )
              } else { // not logged in
                Announcement(size = Announcement.Sizes.Lg)(
                  SiteLink(LoginRoute)("Login"),
                  " to start your own profile."
                )
              }
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
    .componentDidMount(x => x.backend.handleNewProps(x.props))
    .componentWillReceiveProps(x => x.backend.handleNewProps(x.nextProps))
    .build

  def apply(route: ProfileRoute) =
    component.withProps(Props(route.username))
}
