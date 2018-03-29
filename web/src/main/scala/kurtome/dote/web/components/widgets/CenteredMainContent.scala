package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.utils.BaseBackend
import scalacss.internal.mutable.StyleSheet
import wvlet.log.LogSupport

object CenteredMainContent extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val padding = style(
      paddingLeft(SharedStyles.spacingUnit * 2),
      paddingRight(SharedStyles.spacingUnit * 2)
    )

  }

  case class Props()
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def render(p: Props, s: State, children: PropsChildren): VdomElement = {
      GridContainer(justify = Grid.Justify.Center, spacing = 0)(
        GridItem(style = Styles.padding, xs = 12, sm = 10, md = 10, lg = 8, xl = 6)(children)
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .build

  def apply()(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props())
}