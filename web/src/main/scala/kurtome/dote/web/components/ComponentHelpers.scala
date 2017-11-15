package kurtome.dote.web.components

import japgolly.scalajs.react.vdom.Attr
import japgolly.scalajs.react.vdom.Attr.ValueType
import kurtome.dote.web.utils.Linkify

import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.annotation.{JSImport, JSName}
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

  implicit val style2value: Attr.ValueType[StyleA, String] =
    ValueType((fn, style: StyleA) => fn(style.className.value))

  implicit def style2classname(style: StyleA): js.UndefOr[String] = {
    style.className.value
  }

  /**
    * https://github.com/punkave/sanitize-html
    */
  @JSImport("sanitize-html", JSImport.Namespace)
  @js.native
  object SanitizeHtml extends js.Function1[String, String] {
    def apply(rawHtml: String): String = js.native
  }

  @js.native
  trait DangerousInnerHtml extends js.Object {
    var __html: String = js.native
  }

  def sanitizeForReact(html: String): DangerousInnerHtml = {
    val cleanHtml = SanitizeHtml(html)
    val htmlWrapper = (new js.Object).asInstanceOf[DangerousInnerHtml]
    htmlWrapper.__html = cleanHtml
    htmlWrapper
  }

  def linkifyAndSanitize(html: String): DangerousInnerHtml = {
    val linkifiedHtml = Linkify.linkifyHtml(html)
    val cleanHtml = SanitizeHtml(linkifiedHtml)
    val htmlWrapper = (new js.Object).asInstanceOf[DangerousInnerHtml]
    htmlWrapper.__html = cleanHtml
    htmlWrapper
  }
}
