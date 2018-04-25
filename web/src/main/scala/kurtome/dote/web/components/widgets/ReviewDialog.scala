package kurtome.dote.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.get_dotable.GetDotableDetailsRequest
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest.SetReview
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.shared.util.result.StatusCodes
import kurtome.dote.shared.validation.ReviewValidation
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui.Button
import kurtome.dote.web.components.materialui.CircularProgress
import kurtome.dote.web.components.materialui.Dialog
import kurtome.dote.web.components.materialui.DialogActions
import kurtome.dote.web.components.materialui.DialogContent
import kurtome.dote.web.components.materialui.DialogTitle
import kurtome.dote.web.components.materialui.Fade
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui.GridContainer
import kurtome.dote.web.components.materialui.GridItem
import kurtome.dote.web.components.materialui.TextField
import kurtome.dote.web.components.widgets.button.emote.DoteEmoteButton
import kurtome.dote.web.components.widgets.button.emote.DoteStarsButton
import kurtome.dote.web.components.widgets.card.EpisodeCard
import kurtome.dote.web.components.widgets.card.ImageWithSummaryCard
import kurtome.dote.web.components.widgets.card.PodcastCard
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.GlobalLoadingManager
import kurtome.dote.web.utils.GlobalNotificationManager
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object ReviewDialog extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val dialog = style(
      padding(8 px)
    )
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
                   onDoteChanged: (Dote) => Callback)

  case class State(editedReview: String = "",
                   editedDote: Dote = Dote.defaultInstance,
                   reviewError: Boolean = false,
                   reviewHelper: String = "",
                   submitInFlight: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def handleWillReceiveProps(newProps: Props) = Callback {
      if (newProps.open) {
        bs.modState(_.copy(editedDote = newProps.dotable.getDote)).runNow()

        val reviewId = newProps.dotable.getDote.reviewId
        if (reviewId.nonEmpty) {
          bs.modState(_.copy(submitInFlight = true)).runNow()
          DoteProtoServer.getDotableDetails(GetDotableDetailsRequest(reviewId)) map { response =>
            bs.modState(_.copy(submitInFlight = false,
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
                 submitInFlight = result.isSuccess))
        .runNow()
      if (result.isSuccess) {
        val f = DoteProtoServer.setDote(
          SetDoteRequest(p.dotable.id)
            .withDote(s.editedDote)
            .withReview(SetReview(s.editedReview))) map { _ =>
          // Change was successful, let callers know
          p.onDoteChanged(s.editedDote).runNow()
          p.onClose.runNow()
        }
        GlobalLoadingManager.addLoadingFuture(f)
      }
    }

    def render(p: Props, s: State): VdomElement = {
      Dialog(open = p.open,
             onClose = p.onClose,
             style = Styles.dialog,
             maxWidth = Dialog.MaxWidths.Sm,
             fullWidth = true,
             fullScreen = currentBreakpointString == "xs")(
        DialogTitle()("Write Review"),
        DialogContent()(
          if (p.dotable.kind == Dotable.Kind.PODCAST) {
            PodcastCard(dotable = p.dotable, variant = PodcastCard.Variants.Activity)()
          } else {
            EpisodeCard(dotable = p.dotable, variant = EpisodeCard.Variants.Activity)()
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
            disabled = s.submitInFlight,
            placeholder = "Write review..."
          )()
        ),
        DialogActions()(
          GridContainer(justify = Grid.Justify.FlexEnd, alignItems = Grid.AlignItems.Center)(
            GridItem()(
              Fade(in = s.submitInFlight)(
                CircularProgress(variant = CircularProgress.Variant.Indeterminate)())
            ),
            GridItem()(
              Button(onClick = p.onClose, disabled = s.submitInFlight)("Cancel")
            ),
            GridItem()(
              Button(color = Button.Colors.Primary,
                     onClick = attemptPublish(p, s),
                     disabled = s.submitInFlight)("Publish")
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
            onDoteChanged: (Dote) => Callback = _ => Callback.empty) =
    component.withProps(Props(dotable, open, onClose, onDoteChanged))

}
