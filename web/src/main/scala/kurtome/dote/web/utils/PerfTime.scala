package kurtome.dote.web.utils

import org.scalajs.dom
import wvlet.log.LogSupport

object PerfTime extends LogSupport {

  def debugTime[T](label: String)(fn: => T): T = {
    val before = dom.window.performance.now()
    val result = fn
    val after = dom.window.performance.now()
    val total = (after - before).toInt
    if (total > 2) {
      debug(s"timing $label: ${total}ms")
    }
    result
  }

}
