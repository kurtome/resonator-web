package resonator.web.components.widgets.radio

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.web.CssSettings._
import resonator.web.components.materialui.Grid
import resonator.web.components.materialui.GridContainer
import resonator.web.components.materialui.GridItem
import resonator.web.components.materialui.Hidden
import resonator.web.components.materialui.Typography
import resonator.web.constants.MuiTheme
import resonator.web.utils.BaseBackend
import scalacss.internal.mutable.StyleSheet

import scala.scalajs.js

object TunerBand {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val container = style(
      position.relative,
      // border on the right to close the tuner rect
      borderColor.black,
      borderRightStyle.solid,
      borderWidth(1 px),
      height(100 px),
      // leave space for the tick numbers
      marginBottom(1.5 em)
    )

    val tickContainer = style(
      borderColor.black,
      borderTopStyle.solid,
      borderLeftStyle.solid,
      borderBottomStyle.solid,
      borderWidth(1 px),
      height(100 px)
    )

    val minorTickContainer = style(
      position.absolute,
      height(100 px)
    )
  }

  case class Props(minFrequency: Int,
                   maxFrequency: Int,
                   majorTickInterval: Int,
                   currentFrequency: Float)

  class Backend(bs: BackendScope[Props, Unit]) extends BaseBackend(Styles) {

    def render(p: Props): VdomElement = {
      // put a blank tick at the start to make the leftmost line of the tuner
      val displayTicks = Seq("") ++ (p.minFrequency + p.majorTickInterval)
        .until(p.maxFrequency, p.majorTickInterval)
        .map(_.toString)
      val numTicks = displayTicks.size
      val widthPercent = 100.0 / numTicks

      GridContainer(wrap = Grid.Wrap.NoWrap,
                    spacing = 0,
                    justify = Grid.Justify.SpaceBetween,
                    style = Styles.container)(
        Hidden(xsUp = p.currentFrequency < p.minFrequency || p.currentFrequency > p.maxFrequency)(
          <.div(
            ^.position := "absolute",
            ^.width := "5px",
            ^.height := "100px",
            ^.marginLeft := s"${100.0 * ((p.currentFrequency - p.minFrequency) / (p.maxFrequency - p.minFrequency))}%",
            ^.backgroundColor := MuiTheme.theme.palette.secondary.main
          )
        ),
        displayTicks map { tick =>
          GridItem(key = Some(s"tuner-tick-$tick"),
                   style = js.Dynamic.literal("width" -> s"$widthPercent%", "zIndex" -> 1))(
            GridContainer(style = Styles.tickContainer, spacing = 0)(
              GridItem(style = js.Dynamic.literal("width" -> "100%", "height" -> "100%"))(
                <.div(^.height := "100px", ^.width := "100%")),
              GridItem(style = js.Dynamic.literal("width" -> "100%"))(
                <.div(^.height := "40px",
                      ^.width := "1px",
                      ^.marginLeft := "50%",
                      ^.marginTop := "-40px",
                      ^.backgroundColor := "black")),
              GridItem(style = js.Dynamic.literal("position" -> "relative"))(
                Typography(style = js.Dynamic.literal("marginLeft" -> "-50%"))(tick))
            )
          )
        } toVdomArray
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderP((builder, p) => builder.backend.render(p))
    .build

  def apply(minFrequency: Int,
            maxFrequency: Int,
            majorTickInterval: Int,
            currentFrequency: Float) =
    component.withProps(Props(minFrequency, maxFrequency, majorTickInterval, currentFrequency))
}
