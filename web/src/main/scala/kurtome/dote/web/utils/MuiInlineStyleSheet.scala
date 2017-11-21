package kurtome.dote.web.utils

import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._

import scala.scalajs.js
import scalacss.internal.StyleA
import scalacss.internal.mutable.StyleSheet

/**
  * Stylesheet which provides a method to inline ScalaCSS style objects as javascript objects
  * suitble for settings to the "style" attribute on a React component.
  */
class MuiInlineStyleSheet(self: StyleSheet.Inline) {
  private val styleMap: Map[String, js.Dynamic] = styleObjsByClassName(self)

  class RichStyle(style: StyleA) {

    /**
      * Converts a style to its equivalent js representation
      */
    def inline: js.Dynamic = {
      styleMap(style.htmlClass)
    }
  }

  implicit def richStyle(style: StyleA) = new RichStyle(style)
}
