package kurtome.dote.web.utils

import japgolly.scalajs.react.component.Js.UnmountedSimple
import japgolly.scalajs.react.vdom.Attr
import japgolly.scalajs.react.vdom.Attr.ValueType
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.VdomNode
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import wvlet.log.LogSupport

import scala.scalajs.js
import scalacss.internal.StyleA
import scalacss.internal.mutable.StyleSheet

/**
  * Stylesheet which provides a method to inline ScalaCSS style objects as javascript objects
  * suitble for settings to the "style" attribute on a React component.
  */
abstract class BaseBackend(val styleSheet: StyleSheet.Inline) extends LogSupport {

  lazy val styleMap: Map[String, js.Dynamic] = styleObjsByClassName(styleSheet)

  implicit def richStyle(style: StyleA) = styleMap(style.htmlClass)

  implicit def richStyleOrUndef(style: StyleA) = js.UndefOr.any2undefOrA(styleMap(style.htmlClass))

  implicit def vdomNodeOrUndef(node: VdomNode) = js.UndefOr.any2undefOrA(node)

  implicit def vdomElementOrUndef(node: UnmountedSimple[_, _]): js.UndefOr[VdomElement] =
    js.UndefOr.any2undefOrA(node.vdomElement)

  implicit val style2value: Attr.ValueType[StyleA, String] =
    ValueType((fn, style: StyleA) => fn(style.htmlClass))

  implicit def style2string(style: StyleA): String = {
    style.htmlClass
  }

  implicit def style2classname(style: StyleA): js.UndefOr[String] = {
    style.htmlClass
  }

  implicit def func2jsUndefOr[T1, T2, R](
      fn: Function2[T1, T2, R]): js.UndefOr[js.Function2[T1, T2, R]] = {
    js.UndefOr.any2undefOrA(fn)
  }

  styleSheet.addToDocument()
}
