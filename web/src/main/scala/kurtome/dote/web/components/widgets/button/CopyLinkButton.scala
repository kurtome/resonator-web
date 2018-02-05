package kurtome.dote.web.components.widgets.button

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.components.materialui.Button
import kurtome.dote.web.utils.CopyToClipboard
import kurtome.dote.web.utils.GlobalNotificationManager
import org.scalajs.dom

object CopyLinkButton {

  case class Props(copiedMessage: String)

  class Backend(bs: BackendScope[Props, Unit]) {

    val handleShare = (p: Props) =>
      Callback {
        val url = dom.document.location.href
        CopyToClipboard.copyTextToClipboard(url)
        GlobalNotificationManager.displayMessage(p.copiedMessage)
    }

    def render(p: Props): VdomElement = {
      Button(variant = Button.Variants.Raised,
             color = Button.Colors.Secondary,
             onClick = handleShare(p))("Share Link")
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderP((builder, p) => builder.backend.render(p))
    .build

  def apply(copiedMessage: String = "Link copied to your clipboard.") =
    component.withProps(Props(copiedMessage))
}
