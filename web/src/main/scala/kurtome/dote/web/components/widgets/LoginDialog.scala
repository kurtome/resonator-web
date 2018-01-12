package kurtome.dote.web.components.widgets

import dote.proto.api.action.login_link._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.shared.mapper.StatusMapper
import kurtome.dote.web.shared.util.result.ErrorCauses.ErrorCause
import kurtome.dote.web.shared.util.result.StatusCodes.StatusCode
import kurtome.dote.web.shared.util.result._
import kurtome.dote.web.shared.validation.LoginFieldsValidation
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scalacss.internal.mutable.StyleSheet

object LoginDialog extends LogSupport {

  case class Props(routerCtl: DoteRouterCtl, open: Boolean, onClose: Callback)
  case class State(isLoading: Boolean = false,
                   username: String = "",
                   email: String = "",
                   errorMessage: String = "",
                   usernameError: String = "",
                   emailError: String = "")

  private object Styles extends StyleSheet.Inline {
    import dsl._
  }

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

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    val handleSubmit = (p: Props, s: State) =>
      Callback {
        val usernameStatus = LoginFieldsValidation.username.firstError(s.username)
        val emailStatus = LoginFieldsValidation.email.firstError(s.email)

        if (usernameStatus.isSuccess && emailStatus.isSuccess) {
          bs.modState(
              _.copy(isLoading = true, errorMessage = "", emailError = "", usernameError = ""))
            .runNow()
          DoteProtoServer
            .loginLink(LoginLinkRequest(username = s.username, email = s.email))
            .map(handleCreateResponse(p, s))
        } else {
          bs.modState(
              _.copy(errorMessage = "",
                     usernameError = statusToErrorMessage(usernameStatus),
                     emailError = statusToErrorMessage(emailStatus)))
            .runNow()
        }
    }

    private def statusToErrorMessage(status: ActionStatus): String = {
      errorMessages.getOrElse(status.cause, Map()).getOrElse(status.code, "")
    }

    def handleCreateResponse(p: Props, s: State)(response: LoginLinkResponse) = {
      if (response.getStatus.success) {
        bs.modState(
            _.copy(isLoading = false, errorMessage = "", emailError = "", usernameError = ""))
          .runNow()
        GlobalNotificationManager.displayMessage(s"Login link sent to ${s.email}")
        p.onClose.runNow()
      } else {
        val serverStatus = StatusMapper.fromProto(response.getStatus)
        debug(serverStatus)
        val serverErrorMsg = statusToErrorMessage(serverStatus)
        bs.modState(_.copy(isLoading = false, errorMessage = serverErrorMsg))
          .runNow()
      }
    }

    private def sanitizeInput(raw: String): String = {
      raw.toLowerCase.trim
    }

    def handleUsernameChanged(event: ReactEventFromInput) = {
      val newUsername = event.target.value
      bs.modState(_.copy(username = sanitizeInput(newUsername)))
    }

    def handleEmailChanged(event: ReactEventFromInput) = {
      val newEmail = event.target.value
      bs.modState(_.copy(email = sanitizeInput(newEmail)))
    }

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {
      Dialog(open = p.open,
             onEscapeKeyDown = p.onClose,
             onBackdropClick = p.onClose,
             maxWidth = Dialog.MaxWidths.Sm,
             fullWidth = true)(
        DialogTitle(disableTypography = true)(
          Typography(typographyType = Typography.Type.SubHeading)("Login")),
        DialogContent()(
          Typography(typographyType = Typography.Type.Caption, color = Typography.Color.Error)(
            s.errorMessage),
          TextField(
            autoFocus = true,
            fullWidth = true,
            disabled = s.isLoading,
            error = s.usernameError.nonEmpty,
            onChange = handleUsernameChanged,
            helperText = Typography()(<.b(s.usernameError)),
            name = "username",
            label = Typography()("username")
          )(),
          TextField(
            autoFocus = false,
            fullWidth = true,
            disabled = s.isLoading,
            error = s.emailError.nonEmpty,
            onChange = handleEmailChanged,
            helperText = Typography()(<.b(s.emailError)),
            inputType = "email",
            name = "email",
            label = Typography()("email address")
          )()
        ),
        DialogActions()(
          Button(color = Button.Color.Primary, disabled = s.isLoading, onClick = p.onClose)(
            "Cancel"),
          Button(color = Button.Color.Accent,
                 disabled = s.isLoading,
                 onClick = handleSubmit(p, s))("Send login link")
        ),
        Fade(in = s.isLoading)(
          LinearProgress()()
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

  def apply(routerCtl: DoteRouterCtl, open: Boolean, onClose: Callback = Callback.empty)(
      c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(routerCtl, open, onClose))
}
