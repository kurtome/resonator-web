package kurtome.dote.web

import kurtome.dote.web.CssSettings._
import kurtome.dote.web.constants.MuiTheme

import scalacss.internal.mutable.StyleSheet

/**
  * CSS rules for shared classes or elements.
  */
object StandaloneStyles extends StyleSheet.Standalone {
  import dsl._

  "html" - (
    backgroundColor :=! MuiTheme.theme.palette.background.default.asInstanceOf[String]
  )

  "body" - (
    // To override the builtin style from the browser and ensure content goes all the way to the
    // edge of the window
    margin(0 px)
  )

}
