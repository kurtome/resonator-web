package kurtome.dote.web.components

import japgolly.scalajs.react.vdom.Attr
import japgolly.scalajs.react.vdom.Attr.ValueType
import kurtome.dote.web.utils.Linkify

import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.annotation.{JSImport, JSName}
import scalacss.internal.StyleA
import kurtome.dote.web.CssSettings._

import scala.scalajs.js
import scala.scalajs.js.JSON
import scalacss.internal.{Css, CssEntry, Renderer, Style}
import scalacss.internal.mutable.StyleSheet

object ComponentHelpers {

  def epochSecToDate(epochSec: Long): String = {
    if (epochSec > 0) {
      new Date(epochSec * 1000).toLocaleDateString()
    } else {
      ""
    }
  }

  def epochSecToYear(epochSec: Long): Option[String] = {
    if (epochSec > 0) {
      Some(new Date(epochSec * 1000).getFullYear().toString)
    } else {
      None
    }
  }

  def epochSecRangeToYearRange(startEpochSec: Long, endEpochSec: Long): Option[String] = {
    val startYearOpt = epochSecToYear(startEpochSec)
    val endYearOpt = epochSecToYear(endEpochSec)
    startYearOpt map { startYear =>
      endYearOpt map { endYear =>
        if (startYear == endYear) {
          startYear
        } else {
          s"$startYear - $endYear"
        }
      } getOrElse (startYear)
    }
  }

  def durationSecToMin(durationSec: Int): String = {
    if (durationSec > 0) {
      (durationSec / 60) + " min"
    } else {
      ""
    }
  }

  implicit val style2value: Attr.ValueType[StyleA, String] =
    ValueType((fn, style: StyleA) => fn(style.htmlClass))

  implicit def style2string(style: StyleA): String = {
    style.htmlClass
  }

  implicit def style2classname(style: StyleA): js.UndefOr[String] = {
    style.htmlClass
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

  implicit val dynamicRenderer = new Renderer[Map[String, js.Dynamic]] {
    override def apply(css: Css) = {
      css map {
        case style: CssEntry.Style =>
          val jsStyle = js.Dynamic.literal()
          style.content.foreach(kv => {
            jsStyle.updateDynamic(kv.key)(kv.value)
          })
          (style.sel.replace(".", "") -> jsStyle)
        case x => throw new IllegalArgumentException("unsupported " + x.toString)
      } toMap
    }
  }

  /**
    * Renders a stylesheet into a map of javascript objects keyed by classname.
    * This does not support styles with {@code addClassName} method on ScalaCss.
    *
    * @return
    */
  def styleObjsByClassName(styleSheet: StyleSheet.Inline): Map[String, js.Dynamic] = {
    styleSheet.render[Map[String, js.Dynamic]]
  }
}
