package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.util.observer.Observer
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.utils.GlobalNotificationManager
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js
import scalacss.internal.mutable.StyleSheet

object NotificationSnackBar {

  case class Props()
  case class State(message: Option[String] = None, doneDisplaying: Boolean = false) {
    def isOpen = message.isDefined && !doneDisplaying
  }

  object Styles extends StyleSheet.Inline {
    import dsl._
  }

  class Backend(bs: BackendScope[Props, State]) extends LogSupport {

    val stateObserver: Observer[GlobalNotificationManager.Notification] =
      (gs: GlobalNotificationManager.Notification) => {
        bs.modState(_.copy(message = Some(gs.message), doneDisplaying = false)).runNow()
        bs.forceUpdate.runNow()
      }
    GlobalNotificationManager.stateObservable.addObserver(stateObserver)

    def handleSnackbarClose(event: js.Dynamic, reason: String): Callback = Callback {
      bs.modState(_.copy(doneDisplaying = true)).runNow()
    }

    val handleSnackbarCloseClicked: Callback = Callback {
      close()
    }

    val handleWillUnmount: Callback = Callback {
      GlobalNotificationManager.stateObservable.removeObserver(stateObserver)
      close()
    }

    val handleAutoCloseTimeout: () => Unit = () => {
      close()
    }

    def close() = {
      autoCloseTimerId foreach { timeoutId =>
        dom.window.clearTimeout(timeoutId)
      }
      autoCloseTimerId = None
      bs.modState(_.copy(doneDisplaying = true)).runNow()
    }

    var autoCloseTimerId: Option[Int] = None

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {
      val isXs = currentBreakpointString == "xs"

      if (s.message.isDefined && autoCloseTimerId.isEmpty) {
        autoCloseTimerId = Some(dom.window.setTimeout(handleAutoCloseTimeout, 10000))
      }

      val displayMessage = s.message.getOrElse("")
      Snackbar(
        open = s.isOpen,
        // The onClose appears to be broken in the MUI component library, so implementing auto-close
        // timer inthis component
        //onClose = handleSnackbarClose,
        message = <.span(displayMessage).rawElement,
        action = IconButton(color = IconButton.Colors.Secondary,
                            onClick = handleSnackbarCloseClicked)(Icons.Close()).rawElement,
        anchorOrigin = Snackbar.Shape("top", "right")
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
