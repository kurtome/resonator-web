package resonator.web.components.widgets.detail

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.dotable.Dotable
import resonator.web.CssSettings._
import resonator.web.SharedStyles
import resonator.web.audio.AudioPlayer
import resonator.web.components.ComponentHelpers._
import resonator.web.components.lib.Markdown
import resonator.web.components.materialui._
import resonator.web.components.widgets.ActivityHeadline
import resonator.web.components.widgets.ReviewDialog
import resonator.web.components.widgets.SiteLink
import resonator.web.components.widgets.card.EpisodeCard
import resonator.web.components.widgets.card.PodcastCard
import resonator.web.components.widgets.feed.DotableActionsCardWrapper
import resonator.web.constants.MuiTheme
import resonator.web.utils.BaseBackend
import resonator.web.utils.Debounce
import resonator.web.utils.LoggedInPersonManager
import org.scalajs.dom
import scalacss.internal.mutable.StyleSheet

import scala.scalajs.js

object ReviewDetails {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val descriptionText = style(
      marginTop(SharedStyles.spacingUnit)
    )

    val reviewPaper = style(
      padding(8 px)
    )
  }

  case class Props(dotable: Dotable)
  case class State(breakpoint: String, editOpen: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val resizeListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 200) {
      (e: js.Dynamic) =>
        bs.modState(_.copy(breakpoint = currentBreakpointString)).runNow()
    }
    dom.window.addEventListener("resize", resizeListener)

    val handleWillUnmount: Callback = Callback {
      dom.window.removeEventListener("resize", resizeListener)
    }

    def render(p: Props, s: State): VdomElement = {
      val common = p.dotable.getCommon

      val parent = p.dotable.getRelatives.getParent

      val reviewExtras = p.dotable.getReview

      val publishedDate = epochSecToDate(common.publishedEpochSec)
      val editedDate = epochSecToDate(common.updatedEpochSec)
      val editedDateFragment =
        if (publishedDate != editedDate) {
          s" (edited $editedDate)"
        } else {
          ""
        }

      val isCurrentUsersReview =
        LoggedInPersonManager.isLoggedInPerson(reviewExtras.getDote.getPerson.username)

      GridContainer()(
        GridItem(xs = 12, md = 4)(if (parent.kind == Dotable.Kind.PODCAST) {
          PodcastCard(dotable = parent, color = PodcastCard.Colors.PrimaryAccent)()
        } else {
          EpisodeCard(dotable = parent, color = EpisodeCard.Colors.PrimaryAccent)()
        }),
        GridItem(xs = 12, sm = 8, md = 6)(
          Paper(elevation = 0, style = Styles.reviewPaper)(
            GridContainer()(
              GridItem(xs = 12)(
                ActivityHeadline(reviewExtras.getDote)()
              ),
              GridItem(xs = 12)(
                Typography(variant = Typography.Variants.Caption)(
                  s"Reviewed on $publishedDate$editedDateFragment"
                )
              ),
              Hidden(xsUp = !isCurrentUsersReview)(
                GridItem(xs = 12)(
                  Typography(variant = Typography.Variants.Caption)(
                    <.a(
                      ^.color := MuiTheme.theme.palette.primary.light,
                      ^.href := "#",
                      ^.textDecoration := "none",
                      ^.onClick --> bs.modState(_.copy(editOpen = true)),
                      "Edit"
                    )
                  ),
                  ReviewDialog(dotable = parent.withDote(reviewExtras.getDote),
                               open = s.editOpen,
                               onClose = bs.modState(_.copy(editOpen = false)))()
                )
              ),
              GridItem(xs = 12)(
                Typography(component = "div")(Markdown(source = common.description)()))
            )
          )
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State(breakpoint = currentBreakpointString))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .componentWillUnmount(x => x.backend.handleWillUnmount)
    .build

  def apply(p: Props) = component.withKey(p.dotable.id).withProps(p)
}
