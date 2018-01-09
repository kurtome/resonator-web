package kurtome.dote.web.components.widgets

import dote.proto.api.action.login_link._
import dote.proto.api.person.Person
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.shared.util.observer._
import kurtome.dote.web.utils._
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scalacss.internal.mutable.StyleSheet

object LoginDialog extends LogSupport {

  case class Props(routerCtl: DoteRouterCtl, open: Boolean, onClose: Callback)
  case class State(isLoading: Boolean = false,
                   username: String = "",
                   email: String = "",
                   errorMessage: String = "")

  private object Styles extends StyleSheet.Inline {
    import dsl._
  }

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    val handleSubmit = (p: Props, s: State) =>
      Callback {
        bs.modState(_.copy(isLoading = true, errorMessage = "")).runNow()
        DoteProtoServer
          .loginLink(LoginLinkRequest(username = s.username, email = s.email))
          .map(handleCreateResponse(p, s))
    }

    def handleCreateResponse(p: Props, s: State)(response: LoginLinkResponse) = {
      if (response.getStatus.success) {
        bs.modState(_.copy(isLoading = false, errorMessage = "")).runNow()
        GlobalNotificationManager.displayMessage(s"Login link sent to ${s.email}")
        p.onClose.runNow()
      } else {
        bs.modState(_.copy(isLoading = false, errorMessage = response.getStatus.errorMessage))
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
          TextField(autoFocus = true,
                    fullWidth = true,
                    onChange = handleUsernameChanged,
                    name = "username",
                    label = "username",
                    placeholder = "username")(),
          TextField(autoFocus = false,
                    fullWidth = true,
                    onChange = handleEmailChanged,
                    inputType = "email",
                    name = "email address",
                    label = "email address",
                    placeholder = "email address")()
        ),
        DialogActions()(
          Button(color = Button.Color.Primary, onClick = p.onClose)("Cancel"),
          Button(color = Button.Color.Accent, onClick = handleSubmit(p, s))("Send login link")
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
