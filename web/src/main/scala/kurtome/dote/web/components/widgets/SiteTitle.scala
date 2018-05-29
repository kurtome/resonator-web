package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.shared.constants.StringValues
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui.Typography
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils.BaseBackend
import scalacss.internal.mutable.StyleSheet
import wvlet.log.LogSupport

object SiteTitle extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val siteTitleText = style(
      marginTop(16.px),
      fontFamily(SharedStyles.rogueSansExtBoldIt),
      fontStyle.italic,
      fontSize(1.6 rem)
    )

    val siteTitleAnchor = style(
      textDecorationLine.none,
      display.inlineBlock
    )

    val subtitleText = style(
      position.absolute,
      marginTop(-4 px),
      color :=! MuiTheme.theme.palette.grey.`400`
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
      <.div(
        doteRouterCtl.link(HomeRoute)(
          ^.className := Styles.siteTitleAnchor,
          ^.color := MuiTheme.theme.palette.common.white,
          <.span(^.className := Styles.siteTitleText, StringValues.siteTitle),
          Typography(variant = Typography.Variants.Caption, style = Styles.subtitleText)("beta")
        )
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
