package kurtome.dote.web.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.add_podcast._
import kurtome.dote.shared.util.result.ActionStatus
import kurtome.dote.shared.util.result.ErrorCauses
import kurtome.dote.shared.util.result.ErrorCauses.ErrorCause
import kurtome.dote.shared.util.result.StatusCodes
import kurtome.dote.shared.util.result.StatusCodes.StatusCode
import kurtome.dote.shared.validation.AddPodcastValidation
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.Fader
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.GlobalNotificationManager
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success

object AddPodcastDialog extends LogSupport {

  case class Props(open: Boolean, onClose: Callback)
  case class State(errorMessage: String = "",
                   url: String = "",
                   urlError: String = "",
                   response: AddPodcastResponse = AddPodcastResponse.defaultInstance,
                   isLoading: Boolean = false)

  private val errorMessages = Map[ErrorCause, Map[StatusCode, String]](
    ErrorCauses.Url -> Map(
      StatusCodes.Required -> "Username cannot be empty.",
      StatusCodes.InvalidItunesPodcastUrl -> "URL must be an iTunes podcast.",
    )
  )

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    def handleUrlChanged(event: ReactEventFromInput) = Callback {
      val newUrl = event.target.value
      bs.modState(_.copy(url = newUrl)).runNow()
    }

    private def statusToErrorMessage(status: ActionStatus): String = {
      if (status.isSuccess) {
        ""
      } else {
        errorMessages.getOrElse(status.cause, Map()).getOrElse(status.code, "Unexpected error.")
      }
    }

    val handleSubmit = (s: State) =>
      Callback {
        val url = s.url.trim
        val urlStatus = AddPodcastValidation.url.firstError(url)

        if (urlStatus.isSuccess) {
          bs.setState(s.copy(isLoading = true)).runNow
          DoteProtoServer.addPodcast(AddPodcastRequest(itunesUrl = url)) onComplete {
            case Success(apiResponse) => {
              bs.modState(_.copy(response = apiResponse, isLoading = false))
                .runNow()
              apiResponse.podcasts.headOption foreach { podcast =>
                GlobalNotificationManager.displayMessage(s"Added: ${podcast.getCommon.title}")
                doteRouterCtl.set(DetailsRoute(podcast.id, podcast.slug)).runNow()
              }
            }
            case Failure(t) => {
              bs.modState(_.copy(isLoading = false))
              debug("Error occurred.", t)
              GlobalNotificationManager.displayMessage("Network error while adding podcast.")
            }
          }
        } else {
          bs.modState(_.copy(urlError = statusToErrorMessage(urlStatus))).runNow()
        }
    }

    def render(p: Props, s: State): VdomElement =
      Dialog(open = p.open,
             onEscapeKeyDown = p.onClose,
             onBackdropClick = p.onClose,
             maxWidth = Dialog.MaxWidths.Sm,
             fullWidth = true)(
        DialogTitle(disableTypography = true)(
          Typography(typographyType = Typography.Type.SubHeading)("Add Podcast")),
        DialogContent()(
          Typography(typographyType = Typography.Type.Caption, color = Typography.Color.Error)(
            s.errorMessage),
          TextField(
            autoFocus = true,
            fullWidth = true,
            disabled = s.isLoading,
            value = s.url,
            error = s.urlError.nonEmpty,
            onChange = handleUrlChanged,
            helperText = Typography()(<.b(s.urlError)),
            name = "itunes-url",
            label = Typography()("iTunes Podcast URL"),
            placeholder = "https://itunes.apple.com/us/podcast/foocast/id123456789",
          )(),
        ),
        DialogActions()(
          Button(color = Button.Color.Accent, onClick = handleSubmit(s))("Submit")
        ),
        Fader(in = s.isLoading)(
          LinearProgress()()
        )
      )
  }

  val component = ScalaComponent
    .builder[Props]("AddPodcastView")
    .initialState(State())
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .build

  def apply(open: Boolean, onClose: Callback) = component.withProps(Props(open, onClose))
}
