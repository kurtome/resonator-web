package resonator.web.components

import resonator.proto.api.dotable.Dotable
import resonator.web.utils.Linkify

import scala.scalajs.js.Date
import scala.scalajs.js.annotation.{JSImport}
import resonator.web.CssSettings._
import resonator.web.constants.MuiTheme
import org.scalajs.dom
import org.scalajs.dom.html.Div

import scala.scalajs.js
import scalacss.internal.{Css, CssEntry, Renderer}
import scalacss.internal.mutable.StyleSheet

object ComponentHelpers {

  def dotableUrl(dotable: Dotable): String = {
    s"${dom.document.location.protocol}//${dom.document.location.host}/details/${dotable.id}/${dotable.slug}"
  }

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
      val hours = durationSec / 3600
      val minutes = (durationSec % 3600) / 60
      if (hours > 0) {
        s"$hours hr $minutes min"
      } else {
        s"$minutes min"
      }
    } else {
      ""
    }
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
    val linkifiedHtml =
      Linkify.linkifyHtml(html.replace("(\\\\s*<[Bb][Rr]\\\\s*/?>)+\\\\s*$", ""))
    val cleanHtml = SanitizeHtml(linkifiedHtml)
    val htmlWrapper = (new js.Object).asInstanceOf[DangerousInnerHtml]
    htmlWrapper.__html = cleanHtml
    htmlWrapper
  }

  /**
    * snake-case-string to camelCaseString
    *
    * <p>Note that a leading - will produce a leading capital, for example:
    *   -webkit-animation-delay  =>  WebkitAnimationDelay
    */
  def snakeToCamel(snake: String): String = {
    snake
      .split("-")
      .zipWithIndex
      .foldRight("")((strAndIndex, word) =>
        if (strAndIndex._2 == 0) strAndIndex._1 + word else strAndIndex._1.capitalize + word)
  }

  implicit val dynamicRenderer = new Renderer[Map[String, js.Dynamic]] {
    override def apply(css: Css) = {
      css map {
        case style: CssEntry.Style =>
          val jsStyle = js.Dynamic.literal()
          style.content.foreach(kv => {
            // js representation of CSS rules should be camel case, like lineHeight, etc.
            val jsKey = snakeToCamel(kv.key)
            jsStyle.updateDynamic(jsKey)(kv.value)
          })
          style.sel.replace(".", "") -> jsStyle
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

  val breakpointKeys = MuiTheme.theme.breakpoints.keys
  val breakpoints = MuiTheme.theme.breakpoints.values

  def isBreakpointXs: Boolean = currentBreakpointString == "xs"

  def currentBreakpointString = {
    val width = dom.window.innerWidth
    breakpointKeys.reverse
      .find(v => width > breakpoints(v))
      .getOrElse("xs")
  }

  def mergeJSObjects(objs: js.Dynamic*): js.Dynamic = {
    val result = js.Dictionary.empty[Any]
    for (source <- objs) {
      for ((key, value) <- source.asInstanceOf[js.Dictionary[Any]])
        result(key) = value
    }
    result.asInstanceOf[js.Dynamic]
  }

  def asPxStr(px: Int): String = s"${px}px"

  def stripTags(str: String): String = {
    val div = dom.document.createElement("div").asInstanceOf[Div]
    div.innerHTML = str
    div.textContent
  }
}
