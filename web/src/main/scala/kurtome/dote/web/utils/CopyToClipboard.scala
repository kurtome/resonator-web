package kurtome.dote.web.utils

import org.scalajs.dom
import org.scalajs.dom.html.TextArea
import wvlet.log.LogSupport

import scala.util.Try

/**
  * Adapted from https://stackoverflow.com/a/33928558
  */
object CopyToClipboard extends LogSupport {

  def copyTextToClipboard(text: String): Unit = {
    val textArea = dom.document.createElement("textarea").asInstanceOf[TextArea]

    //
    // *** This styling is an extra step which is likely not required. ***
    //
    // Why is it here? To ensure:
    // 1. the element is able to have focus and selection.
    // 2. if element was to flash render it has minimal visual impact.
    // 3. less flakyness with selection and copying which **might** occur if
    //    the textarea element is not visible.
    //
    // The likelihood is the element won't even render, not even a flash,
    // so some of these are just precautions. However in IE the element
    // is visible whilst the popup box asking the user for permission for
    // the web page to copy to the clipboard.
    //

    // Place in top-left corner of screen regardless of scroll position.
    val style =
      "position='fixed' top=-100 left=-100 width=0 height=0 padding=0 border=0 outline=0 box-shadow=0 background='transparent'"
    textArea.setAttribute("style", style)

    textArea.value = text

    dom.document.body.appendChild(textArea);

    textArea.select()

    Try {
      val successful = dom.document.execCommand("copy");
      var msg = if (successful) "successful" else "unsuccessful"
      debug("Copying text command was " + msg);
    } recover {
      case t => {
        warn("Oops, unable to copy", t)
        GlobalNotificationManager.displayError("Something went wrong attempting to copy.")
      }
    }

    dom.document.body.removeChild(textArea)
  }

}
