package resonator.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import resonator.web.CssSettings._
import resonator.web.SharedStyles
import resonator.web.components.ComponentHelpers._
import resonator.web.components.lib.SwipeableViews
import resonator.web.components.materialui.Grid
import resonator.web.components.materialui._
import resonator.web.components.widgets.button.PageArrowButton
import resonator.web.utils.BaseBackend
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

import scala.language.postfixOps
import scala.scalajs.js

object CompactListPagination extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val swipeRoot = style(
      position.relative,
      width :=! "calc(100% + 16px)",
      marginLeft(-8 px),
      marginRight(-8 px)
    )

    val pageButtonGridContainer = style(
      height(100 %%)
    )
  }
  Styles.addToDocument()

  case class Props(pageIndex: Int, onIndexChanged: (Int) => Callback)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def render(p: Props, pc: PropsChildren, s: State): VdomElement = {
      val numPages = pc.count

      val canPrevPage = p.pageIndex != 0 && numPages > 0
      val canNextPage = p.pageIndex != (numPages - 1)

      <.div(
        ^.position := "relative",
        Hidden(xsDown = true, smUp = !(canPrevPage || canNextPage))(
          <.div(
            ^.position := "absolute",
            ^.marginLeft := "-64px",
            ^.height := "100%",
            ^.width := "calc(100% + 128px)",
            GridContainer(style = Styles.pageButtonGridContainer,
                          justify = Grid.Justify.SpaceBetween,
                          alignItems = Grid.AlignItems.Center,
                          spacing = 0)(
              GridItem()(
                Fader(in = canPrevPage)(
                  PageArrowButton(direction = PageArrowButton.Directions.Previous,
                                  disabled = !canPrevPage,
                                  onClick = p.onIndexChanged(Math.max(0, p.pageIndex - 1)))()
                )),
              GridItem()(
                Fader(in = canNextPage)(
                  PageArrowButton(
                    direction = PageArrowButton.Directions.Next,
                    disabled = !canNextPage,
                    onClick = p.onIndexChanged(Math.min(p.pageIndex + 1, numPages))
                  )(Icons.ChevronRight())
                )
              )
            )
          )
        ),
        <.div(
          ^.className := Styles.swipeRoot,
          SwipeableViews(index = p.pageIndex,
                         onIndexChanged = (index, _, _) => p.onIndexChanged(index))(pc)
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPCS((builder, props, propsChildren, state) =>
      builder.backend.render(props, propsChildren, state))
    .build

  def apply(pageIndex: Int = 0, onIndexChanged: (Int) => Callback = (_) => Callback.empty) = {
    component.withProps(Props(pageIndex, onIndexChanged))
  }

}
