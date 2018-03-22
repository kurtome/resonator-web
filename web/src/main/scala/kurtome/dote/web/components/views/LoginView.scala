package kurtome.dote.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.login_link._
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.ErrorCauses.ErrorCause
import kurtome.dote.shared.util.result.StatusCodes.StatusCode
import kurtome.dote.shared.util.result._
import kurtome.dote.shared.validation.LoginFieldsValidation
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils._
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scalacss.internal.mutable.StyleSheet

object LoginView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val paperContainer = style(
      padding(SharedStyles.spacingUnit * 2)
    )
  }

  case class Props()
  case class State(isLoading: Boolean = false,
                   username: String = "",
                   email: String = "",
                   errorMessage: String = "",
                   usernameError: String = "",
                   emailError: String = "")

  private val errorMessages = Map[ErrorCause, Map[StatusCode, String]](
    ErrorCauses.Username -> Map(
      StatusCodes.Required -> "Username cannot be empty.",
      StatusCodes.UnderMin -> "Username minimum length is 4.",
      StatusCodes.OverMax -> "Username maximum length is 15.",
      StatusCodes.InvalidUsername -> "Username can only contain letters and dashes.",
      StatusCodes.NotUnique -> "Username is taken, pick another."
    ),
    ErrorCauses.EmailAddress -> Map(
      StatusCodes.Required -> "Email address cannot be empty.",
      StatusCodes.InvalidEmail -> "Email address is not valid.",
      StatusCodes.NotUnique -> "Email address is in use with different username. If this is your email address check your email for a login link."
    )
  )

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleSubmit = (p: Props, s: State) =>
      Callback {
        val usernameStatus = LoginFieldsValidation.username.firstError(s.username)
        val emailStatus = LoginFieldsValidation.email.firstError(s.email)

        if (usernameStatus.isSuccess && emailStatus.isSuccess) {
          bs.modState(
              _.copy(isLoading = true, errorMessage = "", emailError = "", usernameError = ""))
            .runNow()
          val f = DoteProtoServer
            .loginLink(LoginLinkRequest(username = s.username, email = s.email))
            .map(handleCreateResponse(p, s))
          GlobalLoadingManager.addLoadingFuture(f)
        } else {
          bs.modState(
              _.copy(errorMessage = "",
                     usernameError = statusToErrorMessage(usernameStatus),
                     emailError = statusToErrorMessage(emailStatus)))
            .runNow()
        }
    }

    val handleLogout = Callback {
      dom.document.location.assign("/logout")
    }

    private def statusToErrorMessage(status: ActionStatus): String = {
      if (status.isSuccess) {
        ""
      } else {
        errorMessages.getOrElse(status.cause, Map()).getOrElse(status.code, "Unexpected error.")
      }
    }

    def handleCreateResponse(p: Props, s: State)(response: LoginLinkResponse) = {
      if (response.getResponseStatus.success) {
        bs.modState(
            _.copy(isLoading = false, errorMessage = "", emailError = "", usernameError = ""))
          .runNow()
        GlobalNotificationManager.displayMessage(s"Login link sent to ${s.email}")
        doteRouterCtl.set(HomeRoute).runNow()
      } else {
        val serverStatus = StatusMapper.fromProto(response.getResponseStatus)
        val serverErrorMsg = statusToErrorMessage(serverStatus)
        bs.modState(_.copy(isLoading = false, errorMessage = serverErrorMsg)).runNow()
      }
    }

    def handleUsernameChanged(event: ReactEventFromInput) = Callback {
      val newUsername = event.target.value
      bs.modState(_.copy(username = newUsername.toLowerCase)).runNow()
    }

    def handleUsernameKeyPress(p: Props, s: State)(event: ReactKeyboardEventFromInput) = Callback {
      if (event.key == "Enter") {
        handleSubmit(p, s).runNow()
      }
    }

    def handleEmailChanged(event: ReactEventFromInput) = Callback {
      val newEmail = event.target.value
      bs.modState(_.copy(email = newEmail.toLowerCase)).runNow()
    }

    def handleEmailKeyPress(p: Props, s: State)(event: ReactKeyboardEventFromInput) = Callback {
      if (event.key == "Enter") {
        handleSubmit(p, s).runNow()
      }
    }

    def renderActions(p: Props, s: State): VdomElement = {
      GridContainer(justify = Grid.Justify.FlexEnd, alignItems = Grid.AlignItems.Baseline)(
        GridItem()(
          Button(color = Button.Colors.Primary,
                 variant = Button.Variants.Raised,
                 disabled = s.isLoading,
                 onClick = handleSubmit(p, s))("Submit"))
      )
    }

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {
      GridContainer(spacing = 0, justify = Grid.Justify.Center)(
        GridItem(xs = 12, sm = 10, md = 8)(
          Paper(style = Styles.paperContainer)(
            GridContainer()(
              GridItem(xs = 12)(
                Typography(variant = Typography.Variants.Body1)(
                  "A login link will be emailed to you. Enter your username and email below.")
              ),
              GridItem(xs = 12)(
                Typography(variant = Typography.Variants.Caption, color = Typography.Colors.Error)(
                  s.errorMessage),
                TextField(
                  autoFocus = true,
                  fullWidth = true,
                  disabled = s.isLoading,
                  value = s.username,
                  error = s.usernameError.nonEmpty,
                  onChange = handleUsernameChanged,
                  onKeyPress = handleUsernameKeyPress(p, s),
                  helperText = Typography(component = "span")(<.b(s.usernameError)),
                  inputType = "text",
                  autoComplete = "username",
                  name = "username",
                  label = Typography()("username")
                )(),
                TextField(
                  autoFocus = false,
                  fullWidth = true,
                  disabled = s.isLoading,
                  value = s.email,
                  error = s.emailError.nonEmpty,
                  onChange = handleEmailChanged,
                  onKeyPress = handleEmailKeyPress(p, s),
                  helperText = Typography(component = "span")(<.b(s.emailError)),
                  autoComplete = "email",
                  inputType = "email",
                  name = "email",
                  label = Typography()("email address")
                )()
              ),
              GridItem(xs = 12)(
                renderActions(p, s)
              )
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
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .build

  def apply()(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props())
}
