package kurtome.dote.web.utils

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

  styleSheet.addToDocument()
}
