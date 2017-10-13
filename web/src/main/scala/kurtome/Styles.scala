package kurtome

import CssSettings._

object Styles extends StyleSheet.Inline {
  import dsl._

  val spacingUnit = 8 px

  val paperContainer = style(
    padding(spacingUnit * 2)
  )

  val tileContainer = style(
    )

  val nestedImg = style(
    display.block
  )
}
