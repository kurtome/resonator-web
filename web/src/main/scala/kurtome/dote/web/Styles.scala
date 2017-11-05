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

  val centerTextContainer = style(
    alignItems.center,
    display.flex
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

  val plainAnchor = style(
    textDecorationLine.none,
  )

  val centerText = style(
    textAlign.left,
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

  val cutePunkFf = fontFace("cutePunk")(
    _.src("url(/assets/fonts/cutepunk/Cutepunk_Regular.otf)")
  )

  val jaapokkiRegularFf = fontFace("jaapokkiRegular")(
    _.src("url(/assets/fonts/jaapokki/regular/web/jaapokki-regular.ttf)")
  )

  val jaapokkiEnchanceFf = fontFace("jaapokkiEnchance")(
    _.src("url(/assets/fonts/jaapokki/enchance/web/jaapokkienchance-regular.ttf)")
  )

  val jaapokkiSubtractFf = fontFace("jaapokkiSubtract")(
    _.src("url(/assets/fonts/jaapokki/subtract/web/jaapokkienchance-regular.ttf)")
  )

  val syntesiaFf = fontFace("syntesia")(
    _.src("url(/assets/fonts/syntesia/syntesia.ttf)")
  )

  val rudeFf = fontFace("rude")(
    _.src("url(/assets/fonts/rude/rude.ttf)")
  )

  val aileronHeavyFf = fontFace("aileronHeavy")(
    _.src("url(/assets/fonts/aileron/Aileron-Heavy.ttf)")
  )

  val aileronSemiBoldFf = fontFace("aileronSemiBold")(
    _.src("url(/assets/fonts/aileron/Aileron-SemiBold.ttf)")
  )

  val aileronRegularFf = fontFace("aileronRegular")(
    _.src("url(/assets/fonts/aileron/Aileron-Regular.ttf)")
  )

  val siteTitle = style(
    fontFamily(jaapokkiEnchanceFf),
    textAlign.center,
    paddingTop(60 px)
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
