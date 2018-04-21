package kurtome.dote.web.components.widgets.detail

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.audio.AudioPlayer
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.ActivityHeadline
import kurtome.dote.web.components.widgets.card.EpisodeCard
import kurtome.dote.web.components.widgets.card.PodcastCard
import kurtome.dote.web.components.widgets.feed.DotableActionsCardWrapper
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.Debounce
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
  case class State(breakpoint: String)

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

      GridContainer()(
        GridItem(xs = 12, md = 4)(if (parent.kind == Dotable.Kind.PODCAST) {
          PodcastCard(dotable = parent, variant = PodcastCard.Variants.Activity)()
        } else {
          EpisodeCard(dotable = parent, variant = EpisodeCard.Variants.Activity)()
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
              GridItem(xs = 12)(Typography()(common.description))
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
