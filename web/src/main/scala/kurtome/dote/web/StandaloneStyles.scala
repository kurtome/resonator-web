package kurtome.dote.web

import kurtome.dote.web.CssSettings._

import scalacss.internal.mutable.StyleSheet

/**
  * CSS rules for shared classes or elements.
  */
object StandaloneStyles extends StyleSheet.Standalone {
  import dsl._

  "body" - (
    // To override the builtin style from the browser and ensure content goes all the way to the
    // edge of the window
    margin(0 px)
  )

}
