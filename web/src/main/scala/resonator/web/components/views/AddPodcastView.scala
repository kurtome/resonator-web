package resonator.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.action.add_podcast._
import resonator.shared.util.result.ActionStatus
import resonator.shared.util.result.ErrorCauses
import resonator.shared.util.result.ErrorCauses.ErrorCause
import resonator.shared.util.result.StatusCodes
import resonator.shared.util.result.StatusCodes.StatusCode
import resonator.shared.validation.AddPodcastValidation
import resonator.web.CssSettings._
import resonator.web.DoteRoutes._
import resonator.web.SharedStyles
import resonator.web.components.materialui._
import resonator.web.components.widgets.MainContentSection
import resonator.web.rpc.ResonatorApiClient
import resonator.web.utils.BaseBackend
import resonator.web.utils.GlobalLoadingManager
import resonator.web.utils.GlobalNotificationManager
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure
import scala.util.Success
import scalacss.internal.mutable.StyleSheet

object AddPodcastView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val paperContainer = style(
      padding(SharedStyles.spacingUnit * 2)
    )
  }

  case class Props()
  case class State(errorMessage: String = "",
                   url: String = "",
                   urlError: String = "",
                   response: AddPodcastResponse = AddPodcastResponse.defaultInstance,
                   isLoading: Boolean = false)

  private val errorMessages = Map[ErrorCause, Map[StatusCode, String]](
    ErrorCauses.Url -> Map(
      StatusCodes.Required -> "Username cannot be empty.",
      StatusCodes.InvalidItunesPodcastUrl -> "URL must be an iTunes podcast."
    )
  )

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

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
          val addFuture = ResonatorApiClient.addPodcast(AddPodcastRequest(itunesUrl = url))
          GlobalLoadingManager.addLoadingFuture(addFuture)

          addFuture onComplete {
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
            }
          }
        } else {
          bs.modState(_.copy(urlError = statusToErrorMessage(urlStatus))).runNow()
        }
    }

    def handleUrlKeyPress(s: State)(event: ReactKeyboardEventFromInput) = Callback {
      if (event.key == "Enter") {
        handleSubmit(s).runNow()
      }
    }

    def render(p: Props, s: State): VdomElement =
      MainContentSection()(
        Paper(style = Styles.paperContainer)(
          GridItem(xs = 12)(Typography(variant = Typography.Variants.SubHeading)("Add Podcast")),
          GridItem(xs = 12)(
            Typography(variant = Typography.Variants.Caption, color = Typography.Colors.Error)(
              s.errorMessage),
            TextField(
              autoFocus = true,
              fullWidth = true,
              disabled = s.isLoading,
              value = s.url,
              error = s.urlError.nonEmpty,
              onChange = handleUrlChanged,
              onKeyPress = handleUrlKeyPress(s),
              helperText = Typography()(<.b(s.urlError)),
              name = "itunes-url",
              label = Typography()("iTunes Podcast URL"),
              placeholder = "https://itunes.apple.com/us/podcast/foocast/id123456789",
            )()
          ),
          GridItem(xs = 12)(
            GridContainer(spacing = 0, justify = Grid.Justify.FlexEnd)(
              Button(variant = Button.Variants.Raised,
                     color = Button.Colors.Primary,
                     onClick = handleSubmit(s))("Submit")
            )
          )
        )
      )

  }

  val component = ScalaComponent
    .builder[Props]("AddPodcastView")
    .initialState(State())
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .build

  def apply() = component.withProps(Props())
}
