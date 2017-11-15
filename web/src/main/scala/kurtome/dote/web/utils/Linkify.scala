package kurtome.dote.web.utils

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * https://github.com/SoapBox/linkifyjs
  */
object Linkify {

  @js.native
  trait LinkifyResult extends js.Object {

    /**
      * Will be one of the following
      * - 'url'
      * - 'email'
      * - 'hashtag' (with Hashtag plugin)
      * - 'mention' (with Mention plugin)
      */
    val `type`: String = js.native

    val value: String = js.native

    val href: String = js.native
  }

  @JSImport("linkifyjs", JSImport.Namespace)
  @js.native
  private object Api extends js.Object {
    def find(input: String, `type`: js.UndefOr[String] = js.undefined): Array[LinkifyResult] =
      js.native

    /**
      * Is the given string a link (or the specified type)
      */
    def test(input: String, linkifyType: String = "url"): Boolean = js.native
  }

  @JSImport("linkifyjs/html", JSImport.Namespace)
  @js.native
  private object LinkifyHtml extends js.Function1[String, String] {
    def apply(rawHtml: String): String = js.native
  }

  val linkifyHtml = LinkifyHtml.apply _

  val find = Api.find _

  val test = Api.test _

}
