package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.ComponentHelpers
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.utils.BaseBackend
import scalacss.internal.mutable.StyleSheet
import wvlet.log.LogSupport

import scala.scalajs.js

object CenteredMainContent extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

  }

  case class Props()
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def render(p: Props, s: State, children: PropsChildren): VdomElement = {
      ReactFragment(
        Hidden(mdUp = true)(
          <.div(
            ^.paddingLeft := "16px",
            ^.paddingRight := "16px",
            ^.position.relative,
            children
          )
        ),
        Hidden(smDown = true, lgUp = true)(
          // md size
          <.div(
            // large margins to fit left/right pagination buttons of CompactItemList
            ^.paddingLeft := "80px",
            ^.paddingRight := "80px",
            ^.position.relative,
            children
          )
        ),
        Hidden(mdDown = true)(
          GridContainer(justify = Grid.Justify.Center, spacing = 0)(
            GridItem(
              lg = 10,
              xl = 6
            )(children)
          )
        )
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
