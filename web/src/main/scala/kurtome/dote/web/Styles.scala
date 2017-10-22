package kurtome.dote.web

import scalacss.internal.mutable.StyleSheet
import CssSettings._

object Styles extends StyleSheet.Inline {
  import dsl._

  val spacingUnit = 8 px

  val paperContainer = style(
    padding(spacingUnit * 2)
  )

  val centerContainer = style(
    margin.auto,
    display.table
  )

  val centerItem = style(
    position.relative,
    margin.auto
  )

  val tileContainer = style(
    marginTop(spacingUnit * 3),
    marginBottom(spacingUnit * 3)
  )

  val detailsRoot = style(
    padding(spacingUnit * 2),
  )

  val titleFieldContainer = style(
    textAlign.center,
    display.grid,
    alignContent.center,
    alignItems.center
  )

  val detailsFieldContainer = style(
    textAlign.left,
    display.grid
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

  val inlineBlock = style(
    display.inlineBlock
  )

  val episodeList = style(
    height(100 %%),
    overflow.auto
  )

  val linearProgress = style(
    width(100 %%)
  )

  val podcastDetailsTabContentsContainer = style(
    height(400 px)
  )
}
