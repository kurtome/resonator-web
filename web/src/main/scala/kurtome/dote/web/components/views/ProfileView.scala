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
import kurtome.dote.web.components.widgets.ContentFrame
import kurtome.dote.web.components.widgets.feed.FeedDotableList
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.CopyToClipboard
import kurtome.dote.web.utils.GlobalLoadingManager
import kurtome.dote.web.utils.GlobalNotificationManager
import kurtome.dote.web.utils.GlobalNotificationManager.Notification
import kurtome.dote.web.utils.LoggedInPersonManager
import kurtome.dote.web.utils.MuiInlineStyleSheet
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object ProfileView extends LogSupport {

  private object Styles extends StyleSheet.Inline with MuiInlineStyleSheet {
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
      display.inline,
    )

    val announcementText = style(
      fontSize(1.5 rem)
    )
  }
  Styles.addToDocument()
  import Styles.richStyle

  case class Props(username: String)
  case class State(feed: Feed = Feed.defaultInstance, requestInFlight: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    val handleNewProps = (p: Props) =>
      Callback {
        debug(s"new props $p")
        val f = DoteProtoServer.getFeed(
          GetFeedRequest(maxItems = 20,
                         maxItemSize = 10,
                         id = Some(FeedId().withProfileId(ProfileId(username = p.username))))) map {
          response =>
            bs.modState(_.copy(feed = response.getFeed)).runNow()
        }
        GlobalLoadingManager.addLoadingFuture(f)
    }

    val handleLogout = Callback {
      dom.document.location.assign("/logout")
    }

    val handleShare = Callback {
      val url = dom.document.location.href
      CopyToClipboard.copyTextToClipboard(url)
      GlobalNotificationManager.displayMessage("Link copied: " + url)
    }

    def renderAccountInfo(p: Props, s: State): VdomElement = {
      val loggedInPerson = if (LoggedInPersonManager.isLoggedIn) {
        LoggedInPersonManager.person.get
      } else {
        Person.defaultInstance
      }

      if (isProfileForLoggedInPerson(p)) {
        Paper(elevation = 2, style = Styles.accountInfoContainer.inline)(
          Grid(container = true,
               justify = Grid.Justify.FlexStart,
               alignItems = Grid.AlignItems.Center)(
            Grid(item = true, xs = 12, style = Styles.accountInfoHeaderContainer.inline)(
              Grid(container = true,
                   justify = Grid.Justify.SpaceBetween,
                   alignItems = Grid.AlignItems.Baseline)(
                Grid(item = true)(
                  Typography(typographyType = Typography.Type.SubHeading)("Your info")
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
            )
          )
        )
      } else {
        <.div()
      }
    }

    private def isProfileForLoggedInPerson(p: Props) = {
      LoggedInPersonManager.isLoggedIn && p.username == LoggedInPersonManager.person.get.username
    }

    def render(p: Props, s: State): VdomElement = {

      Grid(container = true, justify = Grid.Justify.Center)(
        Grid(item = true, xs = 12)(
          renderAccountInfo(p, s)
        ),
        Grid(item = true, xs = 12)(
          Grid(container = true, justify = Grid.Justify.FlexStart)(
            Grid(item = true, xs = 12)(
              Typography(typographyType = Typography.Type.Headline,
                         style = Styles.profileHeader.inline)(s"${p.username}'s profile"),
              Button(color = Button.Color.Accent, onClick = handleShare)("Share")
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
        Grid(item = true, xs = 12, sm = 10, md = 8)(
          if (isProfileForLoggedInPerson(p)) {
            Typography(typographyType = Typography.Type.Body1,
                       style = Styles.announcementText.inline)(
              "Your recent activity will show up on your profile. Find something new from the ",
              doteRouterCtl.link(HomeRoute)(^.className := SharedStyles.siteLink,
                                            "popular podcasts"),
              " or try ",
              doteRouterCtl.link(SearchRoute)(^.className := SharedStyles.siteLink,
                                              "searching for a podcast"),
              " you already love."
            )
          } else if (LoggedInPersonManager.isLoggedIn) {
            Typography(typographyType = Typography.Type.Body1,
                       style = Styles.announcementText.inline)(
              "Checkout your own ",
              doteRouterCtl.link(ProfileRoute(LoggedInPersonManager.person.get.username))(
                ^.className := SharedStyles.siteLink,
                "profile page"),
              "."
            )
          } else { // not logged in
            Typography(typographyType = Typography.Type.Body1,
                       style = Styles.announcementText.inline)(
              doteRouterCtl.link(LoginRoute)(^.className := SharedStyles.siteLink, "Login"),
              " to start your own profile."
            )
          }
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
