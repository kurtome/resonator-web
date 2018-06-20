package resonator.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.action.get_dotable.GetDotableDetailsRequest
import resonator.proto.api.action.set_dote.SetDoteRequest
import resonator.proto.api.action.set_dote.SetDoteRequest.SetReview
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.dotable.Dotable.ReviewExtras
import resonator.proto.api.dote.Dote
import resonator.shared.util.result.StatusCodes
import resonator.shared.validation.ReviewValidation
import resonator.web.CssSettings._
import resonator.web.components.ComponentHelpers._
import resonator.web.components.materialui.Button
import resonator.web.components.materialui.CircularProgress
import resonator.web.components.materialui.Dialog
import resonator.web.components.materialui.DialogActions
import resonator.web.components.materialui.DialogContent
import resonator.web.components.materialui.DialogTitle
import resonator.web.components.materialui.Fade
import resonator.web.components.materialui.Grid
import resonator.web.components.materialui.GridContainer
import resonator.web.components.materialui.GridItem
import resonator.web.components.materialui.TextField
import resonator.web.components.widgets.button.emote.DoteEmoteButton
import resonator.web.components.widgets.button.emote.DoteStarsButton
import resonator.web.components.widgets.card.EpisodeCard
import resonator.web.components.widgets.card.PodcastCard
import resonator.web.rpc.ResonatorApiClient
import resonator.web.utils.BaseBackend
import resonator.web.utils.GlobalLoadingManager
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object ReviewDialog extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

  }

  object Variants extends Enumeration {
    val Accent = Value // recent activity feed
    val CardHeader = Value // recent activity feed
    val Default = Value
  }
  type Variant = Variants.Value

  case class Props(dotable: Dotable,
                   open: Boolean,
                   onClose: Callback,
                   onReviewChanged: (ReviewExtras) => Callback)

  case class State(editedReview: String = "",
                   editedDote: Dote = Dote.defaultInstance,
                   reviewError: Boolean = false,
                   reviewHelper: String = "",
                   loading: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def handleWillReceiveProps(newProps: Props) = Callback {
      if (newProps.open) {
        bs.modState(_.copy(editedDote = newProps.dotable.getDote)).runNow()

        val reviewId = newProps.dotable.getDote.reviewId
        if (reviewId.nonEmpty) {
          bs.modState(_.copy(loading = true)).runNow()
          ResonatorApiClient.getDotableDetails(GetDotableDetailsRequest(reviewId)) map {
            response =>
              bs.modState(_.copy(loading = false,
                                 editedReview = response.getDotable.getCommon.description))
                .runNow()
          }
        }
      }
    }

    def handleReviewChanged(event: ReactEventFromInput) = Callback {
      val newValue = event.target.value

      val reviewLength = newValue.size

      val helperMessage =
        if (reviewLength > (ReviewValidation.maxLength - 100)) {
          (-(reviewLength - ReviewValidation.maxLength)).toString
        } else {
          ""
        }

      val reviewError = reviewLength > ReviewValidation.maxLength

      bs.modState(
          _.copy(editedReview = newValue, reviewHelper = helperMessage, reviewError = reviewError))
        .runNow()
    }

    def doteChanged(newDote: Dote) = {
      bs.modState(_.copy(editedDote = newDote))
    }

    def attemptPublish(p: Props, s: State) = Callback {
      val result = ReviewValidation.body.firstError(s.editedReview)
      val message =
        if (result.isSuccess) {
          ""
        } else {
          result.code match {
            case StatusCodes.Required => "Review cannot be empty."
            case StatusCodes.UnderMin => "Review is too short."
            case StatusCodes.OverMax =>
              s"Review cannot be over ${ReviewValidation.maxLength} characters."
            case _ => "Unknown error."
          }
        }
      bs.modState(
          _.copy(reviewError = message.nonEmpty,
                 reviewHelper = message,
                 loading = result.isSuccess))
        .runNow()
      if (result.isSuccess) {
        val f = ResonatorApiClient.setDote(
          SetDoteRequest(p.dotable.id)
            .withDote(s.editedDote)
            .withReview(SetReview(s.editedReview))) map { _ =>
          // Change was successful, let callers know
          p.onReviewChanged(ReviewExtras().withDote(s.editedDote)).runNow()
          p.onClose.runNow()
        }
        GlobalLoadingManager.addLoadingFuture(f)
      }
    }

    def render(p: Props, s: State): VdomElement = {
      Dialog(open = p.open,
             onClose = p.onClose,
             maxWidth = Dialog.MaxWidths.Sm,
             fullWidth = true,
             fullScreen = currentBreakpointString == "xs")(
        DialogTitle()("Write Review"),
        DialogContent()(
          if (p.dotable.kind == Dotable.Kind.PODCAST) {
            PodcastCard(dotable = p.dotable, color = PodcastCard.Colors.PrimaryAccent)()
          } else {
            EpisodeCard(dotable = p.dotable, color = EpisodeCard.Colors.PrimaryAccent)()
          },
          GridContainer(spacing = 8,
                        justify = Grid.Justify.SpaceAround,
                        alignItems = Grid.AlignItems.Center)(
            GridItem()(
              DoteStarsButton(p.dotable.withDote(s.editedDote),
                              onDoteChanged = doteChanged,
                              sendToServer = false)()),
            GridItem()(
              DoteEmoteButton(p.dotable.withDote(s.editedDote),
                              showAllOptions = true,
                              onDoteChanged = doteChanged,
                              sendToServer = false)())
          ),
          TextField(
            fullWidth = true,
            multiline = true,
            onChange = handleReviewChanged,
            error = s.reviewError,
            value = s.editedReview,
            helperText = s.reviewHelper,
            disabled = s.loading,
            placeholder = "Write review..."
          )()
        ),
        DialogActions()(
          GridContainer(justify = Grid.Justify.FlexEnd, alignItems = Grid.AlignItems.Center)(
            GridItem()(
              Fade(in = s.loading)(
                CircularProgress(variant = CircularProgress.Variant.Indeterminate)())
            ),
            GridItem()(
              Button(onClick = p.onClose, disabled = s.loading)("Cancel")
            ),
            GridItem()(
              Button(color = Button.Colors.Primary,
                     onClick = attemptPublish(p, s),
                     disabled = s.loading)("Publish")
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
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .componentWillReceiveProps(x => x.backend.handleWillReceiveProps(x.nextProps))
    .componentWillMount(x => x.backend.handleWillReceiveProps(x.props))
    .build

  def apply(dotable: Dotable,
            open: Boolean,
            onClose: Callback = Callback.empty,
            onReviewChanged: (ReviewExtras) => Callback = _ => Callback.empty) =
    component.withProps(Props(dotable, open, onClose, onReviewChanged))

}
