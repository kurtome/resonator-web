package resonator.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.shared.util.observer.Observer
import resonator.web.CssSettings._
import resonator.web.components.ComponentHelpers._
import resonator.web.components.materialui._
import resonator.web.utils.GlobalNotificationManager
import resonator.web.utils.GlobalNotificationManager.Notification
import resonator.web.utils.GlobalNotificationManager.NotificationKinds
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js
import scalacss.internal.mutable.StyleSheet

object NotificationSnackBar {

  case class Props()
  case class State(message: Option[Notification] = None, doneDisplaying: Boolean = false) {
    def isOpen = message.isDefined && !doneDisplaying
  }

  object Styles extends StyleSheet.Inline {
    import dsl._
  }

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    val stateObserver: Observer[GlobalNotificationManager.Notification] =
      (gs: GlobalNotificationManager.Notification) => {
        bs.modState(_.copy(message = Some(gs), doneDisplaying = false)).runNow()
        bs.forceUpdate.runNow()
      }
    GlobalNotificationManager.stateObservable.addObserver(stateObserver)

    def handleSnackbarOnClose(event: js.Dynamic, reason: String): Callback = Callback {
      // notifcation snacks should only auto-close if autoHideDurationMs elapses (ignore the
      // clickaway close)
      if (reason == "timeout") {
        close()
      }
    }

    val handleSnackbarCloseClicked: Callback = Callback {
      close()
    }

    val handleWillUnmount: Callback = Callback {
      GlobalNotificationManager.stateObservable.removeObserver(stateObserver)
    }

    def close() = {
      bs.modState(_.copy(doneDisplaying = true)).runNow()
    }

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {
      val isXs = currentBreakpointString == "xs"

      val color = s.message.map(_.kind) match {
        case Some(NotificationKinds.Error) => Typography.Colors.Error
        case _ => Typography.Colors.Inherit
      }

      val displayMessage = s.message.map(_.message).getOrElse("")
      Snackbar(
        open = s.isOpen,
        autoHideDurationMs = 10000,
        onClose = handleSnackbarOnClose,
        message = Typography(color = color)(displayMessage).rawElement,
        action = IconButton(color = IconButton.Colors.Inherit,
                            onClick = handleSnackbarCloseClicked)(Icons.Close()).rawElement,
        anchorOrigin = Snackbar.Shape("bottom", "left")
      )()
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .componentWillUnmount(x => x.backend.handleWillUnmount)
    .build

  def apply()(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props())
}
