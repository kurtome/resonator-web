package kurtome.dote.web.components

import scala.scalajs.js
import scala.scalajs.js.Date
import scalacss.internal.StyleA

object ComponentHelpers {

  def epochSecToDate(epochSec: Long): String = {
    if (epochSec > 0) {
      new Date(epochSec * 1000).toLocaleDateString()
    } else {
      ""
    }
  }

  def epochSecRangeToYearRange(startEpochSec: Long, endEpochSec: Long): String = {
    val startYear = epochSecToYear(startEpochSec)
    val endYear = epochSecToYear(endEpochSec)
    if (startYear == endYear) {
      startYear
    } else {
      s"$startYear - $endYear"
    }
  }

  def epochSecToYear(epochSec: Long): String = {
    if (epochSec > 0) {
      new Date(epochSec * 1000).getFullYear().toString
    } else {
      ""
    }
  }

  def durationSecToMin(durationSec: Int): String = {
    if (durationSec > 0) {
      (durationSec / 60) + " minutes"
    } else {
      ""
    }
  }

  implicit def style2classnameStr(style: StyleA): String = {
    style.className.value
  }

  implicit def style2classname(style: StyleA): js.UndefOr[String] = {
    style.className.value
  }
}
