package resonator.web.utils

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Uses mobile-detect https://github.com/hgoebl/mobile-detect.js
  */
object IsMobile {

  @JSImport("mobile-detect", JSImport.Default)
  @js.native
  private class MobileDetect(useragent: String) extends js.Object {
    def mobile(): String = js.native
  }

  private lazy val md = new MobileDetect(dom.window.navigator.userAgent)

  lazy val value: Boolean = md.mobile() != null && md.mobile().nonEmpty
}
