package kurtome

import CssSettings._

object Styles extends StyleSheet.Inline {
  import dsl._

  val spacingUnit = 8 px

  val paperContainer = style(
    padding(spacingUnit * 2)
  )

  val tileContainer = style(
    marginTop(spacingUnit * 3),
    marginBottom(spacingUnit * 3)
  )

  val detailsContainer = style(
    padding(spacingUnit * 2),
  )

  val detailsTitle = style(
    textAlign.center,
  )

  val nestedImg = style(
    display.block,
    margin.auto // margin: 'auto' make this img centered in its space
  )

  val entityDetailsContainer = style(
    margin(spacingUnit * 2)
  )

  val titleText = style(
    textAlign.center,
    paddingTop(20 px)
  )
}
