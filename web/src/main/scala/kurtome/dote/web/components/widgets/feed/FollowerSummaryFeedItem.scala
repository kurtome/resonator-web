package kurtome.dote.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_follow.SetFollowRequest
import kurtome.dote.proto.api.feed.FeedFollowerSummary
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.proto.api.follower.FollowerSummary
import kurtome.dote.proto.api.person.Person
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.MainContentSection
import kurtome.dote.web.components.widgets.SiteLink
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.FeedIdRoutes
import kurtome.dote.web.utils.GlobalLoadingManager
import kurtome.dote.web.utils.GlobalNotificationManager
import kurtome.dote.web.utils.LoggedInPersonManager
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object FollowerSummaryFeedItem extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val root = style(
      display.inlineBlock,
      padding(SharedStyles.spacingUnit)
    )

    val followCounterContainer = style(
      display.inlineBlock,
      paddingLeft(SharedStyles.spacingUnit * 2),
      paddingRight(SharedStyles.spacingUnit * 2)
    )

    val tableContainer = style(
      padding(SharedStyles.spacingUnit)
    )

    val tableCell = style(
      padding(SharedStyles.spacingUnit * 2)
    )

    val truncateText = style(
      whiteSpace.nowrap,
      overflow.hidden,
      textOverflow := "ellipsis"
    )
  }
  Styles.addToDocument()

  case class Props(feedItem: FeedItem)
  case class State(followerSummary: FollowerSummary, setFollowInFlight: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    private def isFollowing(s: State): Boolean = {
      LoggedInPersonManager.person map { loggedInPerson =>
        s.followerSummary.followers.exists(_.id == loggedInPerson.id)
      } getOrElse false
    }

    private def isFollowPending(s: State): Boolean = {
      s.followerSummary.person.isEmpty || s.setFollowInFlight
    }

    private def handleFollowingChanged(s: State)(event: ReactEventFromInput) = Callback {
      val checked = event.target.checked
      bs.modState(_.copy(setFollowInFlight = true)).runNow()
      val newState =
        if (checked) SetFollowRequest.State.FOLLOWING else SetFollowRequest.State.NOT_FOLLOWING
      val f = DoteProtoServer.setFollow(
        SetFollowRequest(requesterPersonId = LoggedInPersonManager.person.get.id,
                         followPersonId = s.followerSummary.person.get.id,
                         requestedState = newState)
      ) map { response =>
        if (StatusMapper.fromProto(response.getResponseStatus).isSuccess) {
          bs.modState(_.copy(followerSummary = response.getSummary, setFollowInFlight = false))
            .runNow()
        } else {
          bs.modState(_.copy(setFollowInFlight = false))
            .runNow()
          GlobalNotificationManager.displayError("Error occurred while following.")
        }
      }
      GlobalLoadingManager.addLoadingFuture(f)
    }

    private def counterContainer(p: Props)(vdomElement: VdomElement): VdomElement = {
      FeedIdRoutes.toRoute(p.feedItem.getId) match {
        case Some(route) => SiteLink(route)(vdomElement)
        case _ => vdomElement
      }
    }

    private def renderPersonTable(title: String, people: Seq[Person]): VdomElement = {
      if (people.isEmpty) {
        <.div()
      } else {
        Paper(style = Styles.tableContainer)(
          Table()(
            TableHead()(
              TableRow()(
                TableCell(style = Styles.tableCell)(
                  Typography(variant = Typography.Variants.Title)(title)))
            ),
            TableBody()((people.zipWithIndex map {
              case (person, i) =>
                val id = person.id
                val key: String = if (id.isEmpty) i.toString else id
                val username = person.username
                val detailRoute = ProfileRoute(username)
                TableRow(key = Some(key))(
                  TableCell(style = Styles.tableCell)(
                    Typography(variant = Typography.Variants.Body1, noWrap = true)(
                      SiteLink(detailRoute)(person.username))
                  )
                )
            }).toVdomArray)
          )
        )
      }
    }

    def render(p: Props, s: State): VdomElement = {
      val showFollowCheckbox = (LoggedInPersonManager.isNotLoggedIn
        || LoggedInPersonManager.isLoggedInPerson(s.followerSummary.getPerson.username))

      val summary = p.feedItem.getFollowerSummary.getSummary
      val person = summary.getPerson
      <.div(
        MainContentSection(variant = MainContentSection.Variants.Light)(
          GridContainer(spacing = 0,
                        justify = Grid.Justify.SpaceBetween,
                        alignItems = Grid.AlignItems.FlexStart)(
            GridItem()(
              GridContainer(spacing = 0)(
                GridItem(xs = 12)(
                  SiteLink(ProfileRoute(person.username))(
                    Typography(variant = Typography.Variants.Display2,
                               color = Typography.Colors.Inherit)(person.username)
                  )
                ),
                GridItem(xs = 12, hidden = Grid.HiddenProps(xsUp = showFollowCheckbox))(
                  GridContainer(spacing = 0)(
                    GridItem()(FormControlLabel(
                      control = Checkbox(checked = isFollowing(s),
                                         name = "follow",
                                         value = "follow",
                                         disabled = isFollowPending(s),
                                         onChange = handleFollowingChanged(s))(),
                      label = s"Follow"
                    )()),
                    GridItem()(
                      Fade(in = s.setFollowInFlight)(
                        CircularProgress(variant = CircularProgress.Variant.Indeterminate)()
                      ))
                  )
                )
              )
            ),
            GridItem()(
              <.div(
                ^.className := Styles.followCounterContainer,
                counterContainer(p)(
                  Typography(variant = Typography.Variants.Display1,
                             color = Typography.Colors.Inherit)(s.followerSummary.following.length)
                ),
                Typography(variant = Typography.Variants.Caption)("Following")
              ),
              <.div(
                ^.className := Styles.followCounterContainer,
                counterContainer(p)(
                  Typography(variant = Typography.Variants.Display1,
                             color = Typography.Colors.Inherit)(s.followerSummary.followers.length)
                ),
                Typography(variant = Typography.Variants.Caption)("Followers")
              )
            )
          )
        ),
        MainContentSection()(
          Hidden(xsUp = p.feedItem.getFollowerSummary.style != FeedFollowerSummary.Style.PRIMARY)(
            GridItem(xs = 12)(
              renderPersonTable("Following", s.followerSummary.following),
              renderPersonTable("Followers", s.followerSummary.followers)
            )
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => State(p.feedItem.getFollowerSummary.getSummary))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .build

  def apply(feedItem: FeedItem) = {
    assert(feedItem.getId.id.isFollowerSummary)
    component.withProps(Props(feedItem))
  }

}
