package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.constants.StringValues
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import scalacss.internal.mutable.StyleSheet
import wvlet.log.LogSupport

object SiteTitle extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val siteTitleText = style(
      fontFamily(SharedStyles.rogueSansExtBoldIt),
      fontStyle.italic,
      fontSize(1.9.rem),
      lineHeight(2.5.rem)
    )

    val underConstructionText = style(
      paddingLeft(8.px),
      fontSize(1.rem),
      lineHeight(2.5.rem),
      position.absolute
    )

    val siteTitleAnchor = style(
      textDecorationLine.none,
      display.inlineBlock
    )
  }

  object Variants extends Enumeration {
    val Small = Value
    val Large = Value
  }
  type Variant = Variants.Value

  case class Props()
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def render(p: Props): VdomElement = {
      val isXs = currentBreakpointString == "xs"

      <.div(
        doteRouterCtl.link(HomeRoute)(
          ^.className := Styles.siteTitleAnchor,
          ^.color := MuiTheme.theme.palette.common.white,
          <.span(^.className := Styles.siteTitleText, StringValues.siteTitle)
        ),
        <.span(^.className := Styles.underConstructionText,
               ^.color := MuiTheme.secondaryTextColor,
               "(under construction)")
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderP((b, p) => b.backend.render(p))
    .build

  def apply() = component.withProps(Props())
}
