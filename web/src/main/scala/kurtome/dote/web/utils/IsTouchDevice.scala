package kurtome.dote.web.utils

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * https://github.com/airbnb/is-touch-device
  */
object IsTouchDevice {

  @JSImport("is-touch-device", JSImport.Default)
  @js.native
  private object RawFunc extends js.Function0[Boolean] {
    override def apply(): Boolean = js.native
  }

  val value: Boolean = RawFunc()
}
