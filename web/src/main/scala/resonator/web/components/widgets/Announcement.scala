package resonator.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.web.CssSettings._
import resonator.web.SharedStyles
import resonator.web.components.ComponentHelpers._
import resonator.web.components.materialui._
import resonator.web.components.widgets.Announcement.Sizes.Size
import resonator.web.utils.BaseBackend
import wvlet.log.LogSupport

import scalacss.internal.mutable.StyleSheet

/**
  * Pager wrapper includes header/footer and renders child content within a centered portion of the
  * screen.
  */
object Announcement extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val smallAnnouncement = style(
      fontSize(1.3 rem)
    )

    val largeAnnouncement = style(
      fontSize(1.5 rem)
    )

  }

  object Sizes extends Enumeration {
    type Size = Value
    val Sm = Value
    val Lg = Value
  }

  case class Props(size: Size)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    private def textStyle(p: Props) = {
      p.size match {
        case Sizes.Sm => Styles.smallAnnouncement
        case _ => Styles.largeAnnouncement
      }
    }

    def render(p: Props, s: State, mainContent: PropsChildren): VdomElement = {
      <.div(
        Typography(variant = Typography.Variants.Body1, style = textStyle(p))(mainContent)
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPCS((b, p, pc, s) => b.backend.render(p, s, pc))
    .build

  def apply(size: Size = Sizes.Lg)(c: CtorType.ChildArg*) =
    component.withChildren(c: _*)(Props(size))
}
