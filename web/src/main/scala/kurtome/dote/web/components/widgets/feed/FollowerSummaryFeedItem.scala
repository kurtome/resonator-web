package kurtome.dote.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_follow.SetFollowRequest
import kurtome.dote.proto.api.follower.FollowerSummary
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.GlobalLoadingManager
import kurtome.dote.web.utils.GlobalNotificationManager
import kurtome.dote.web.utils.LoggedInPersonManager
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object FollowerSummaryFeedItem extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val root = style(
      display.inlineBlock,
      padding(SharedStyles.spacingUnit)
    )

    val followCounterContainer = styleF.bool(
      isFirst =>
        styleS(
          display.inlineBlock,
          borderLeft(if (isFirst) 0 px else 1 px, solid, Color(MuiTheme.theme.palette.divider)),
          paddingLeft(SharedStyles.spacingUnit * 2),
          paddingRight(SharedStyles.spacingUnit * 2)
      ))
  }
  Styles.addToDocument()

  case class Props(followerSummary: FollowerSummary)
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

    def render(s: State): VdomElement = {
      GridContainer(alignItems = Grid.AlignItems.Center)(
        GridItem()(
          Paper(style = Styles.root)(
            <.div(
              ^.className := Styles.followCounterContainer(true),
              Typography(variant = Typography.Variants.Headline)(
                s.followerSummary.following.length),
              Typography(variant = Typography.Variants.Caption)("Following")
            ),
            <.div(
              ^.className := Styles.followCounterContainer(false),
              Typography(variant = Typography.Variants.Headline)(
                s.followerSummary.followers.length),
              Typography(variant = Typography.Variants.Caption)("Followers")
            )
          )),
        GridItem()(
          if (LoggedInPersonManager.isLoggedInPerson(s.followerSummary.getPerson.username)) {
            <.div()
          } else {
            GridContainer(spacing = 0)(
              GridItem()(
                FormControlLabel(
                  control = Checkbox(checked = isFollowing(s),
                                     name = "follow",
                                     value = "follow",
                                     disabled = isFollowPending(s),
                                     onChange = handleFollowingChanged(s))(),
                  label = Typography()(s"Follow ${s.followerSummary.getPerson.username}")
                )()),
              GridItem()(
                Fade(in = s.setFollowInFlight)(
                  CircularProgress(variant = CircularProgress.Variant.Indeterminate)()
                ))
            )
          })
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => State(p.followerSummary))
    .backend(new Backend(_))
    .renderS((builder, state) => builder.backend.render(state))
    .build

  def apply(followerSummary: FollowerSummary) = {
    component.withProps(Props(followerSummary))
  }

}
