package resonator.web

import resonator.web.CssSettings._
import resonator.web.components.materialui.Colors
import resonator.web.constants.MuiTheme

import scalacss.internal.mutable.StyleSheet

/**
  * CSS rules for shared classes or elements.
  */
//noinspection ScalaUnnecessaryParentheses
class StandaloneStyles extends StyleSheet.Standalone {
  import dsl._

  "html" - (
    backgroundColor :=! MuiTheme.theme.palette.background.default
  )

  "body" - (
    // To override the builtin style from the browser and ensure content goes all the way to the
    // edge of the window
    margin(0 px)
  )

  "a" - (
    color :=! Colors.lightBlue.`500`
  )

  // Hide the "x" icon from the search type text inputs

  """input[type="search"]::-webkit-search-decoration""" - (
    display.none
  )

  """input[type="search"]::-webkit-search-cancel-button""" - (
    display.none
  )

  """input[type="search"]::-webkit-search-results-button""" - (
    display.none
  )

  """input[type="search"]::-webkit-search-results-decoration""" - (
    display.none
  )

  """input[type=search]::-ms-clear""" - (
    display.none
  )

  """input[type=search]::-ms-reveal""" - (
    display.none
  )

}
