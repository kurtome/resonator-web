package resonator.web.components.widgets.button

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.web.components.materialui.IconButton
import resonator.web.components.materialui.Icons
import resonator.web.utils.CopyToClipboard
import resonator.web.utils.GlobalNotificationManager
import org.scalajs.dom

object ShareButton {

  case class Props(shareUrl: String, copiedMessage: String)

  class Backend(bs: BackendScope[Props, Unit]) {

    val handleShare = (p: Props) =>
      Callback {
        val url =
          if (p.shareUrl.nonEmpty) {
            p.shareUrl
          } else {
            dom.document.location.protocol + dom.document.location.href
          }
        CopyToClipboard.copyTextToClipboard(url)
        GlobalNotificationManager.displayMessage(p.copiedMessage)
    }

    def render(p: Props): VdomElement = {
      IconButton(onClick = handleShare(p), color = IconButton.Colors.Primary)(Icons.Share())
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderP((builder, p) => builder.backend.render(p))
    .build

  def apply(shareUrl: String = "", copiedMessage: String = "Link copied to your clipboard.") =
    component.withProps(Props(shareUrl, copiedMessage))
}
