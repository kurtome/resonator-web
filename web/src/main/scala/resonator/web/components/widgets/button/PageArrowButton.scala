package resonator.web.components.widgets.button

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import resonator.web.CssSettings._
import resonator.web.components.materialui.Button
import resonator.web.components.materialui.Icons
import resonator.web.constants.MuiTheme
import resonator.web.utils.BaseBackend
import scalacss.internal.mutable.StyleSheet

object PageArrowButton {

  object Styles extends StyleSheet.Inline {
    import dsl._

    private val roundCornerRadius = 5 px

    val rounded = style(
      minHeight(25 px),
      padding(0 px),
      minWidth.unset,
      borderRadius(roundCornerRadius)
    )

    val fillPrimaryLightButton = style(
      backgroundColor :=! MuiTheme.theme.palette.extras.pageButtonFill,
      borderRadius(roundCornerRadius)
    )

    val whiteText = style(
      fontSize(3 rem),
      color :=! MuiTheme.theme.palette.common.white
    )
  }

  case class Props(direction: Direction, onClick: Callback, disabled: Boolean)

  object Directions extends Enumeration {
    val Next = Value
    val Previous = Value
  }
  type Direction = Directions.Value

  class Backend(bs: BackendScope[Props, Unit]) extends BaseBackend(Styles) {

    def render(p: Props, pc: PropsChildren): VdomElement = {
      <.div(
        ^.className := Styles.fillPrimaryLightButton,
        Button(style = Styles.rounded, onClick = p.onClick, disabled = p.disabled)(
          p.direction match {
            case Directions.Next => Icons.ChevronRight(style = Styles.whiteText)
            case _ => Icons.ChevronLeft(style = Styles.whiteText)
          }
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderPC((builder, props, pc) => builder.backend.render(props, pc))
    .build

  def apply(direction: Direction, onClick: Callback = Callback.empty, disabled: Boolean = false)(
      c: CtorType.ChildArg*) =
    component.withChildren(c: _*).withProps(Props(direction, onClick, disabled))()
}
