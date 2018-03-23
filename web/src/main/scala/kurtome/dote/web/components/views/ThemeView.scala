package kurtome.dote.web.components.views

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.WebMain
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.MainContentSection
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.constants.MuiTheme.PaletteColor
import kurtome.dote.web.utils.BaseBackend
import wvlet.log.LogSupport

object ThemeView extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val paperContainer = style(
      padding(SharedStyles.spacingUnit)
    )
  }

  case class Props()
  case class State(light: Boolean = MuiTheme.theme.palette.paletteType == "light",
                   primary: PaletteColor = MuiTheme.theme.palette.primary,
                   secondary: PaletteColor = MuiTheme.theme.palette.secondary)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleToggleLights = Callback {
      bs.modState(s => {
          val newState = s.copy(light = !s.light)
          setTheme(newState)
          newState
        })
        .runNow()
    }

    def setTheme(s: State) = {
      MuiTheme.set(
        MuiTheme.createTheme(
          light = s.light,
          primary = s.primary,
          secondary = s.secondary
        )
      )
      WebMain.refreshStyles()
    }

    def render(p: Props, s: State): VdomElement = {
      MainContentSection()(
        GridContainer()(
          GridItem(xs = 12)(Button(variant = Button.Variants.Raised, onClick = handleToggleLights)(
            "Toggle Lights")),
          GridItem(xs = 12)(Divider()()),
          GridItem(xs = 12)(Typography()("Default text")),
          GridItem(xs = 12)(Typography(variant = Typography.Variants.Title)("Title text")),
          GridItem(xs = 12)(
            Typography(variant = Typography.Variants.SubHeading)("Subheading text")),
          GridItem(xs = 12)(Typography(variant = Typography.Variants.Body1)("Body1 text")),
          GridItem(xs = 12)(Typography(variant = Typography.Variants.Caption)("Caption text")),
          GridItem(xs = 12)(Typography(color = Typography.Colors.Primary)("Primary color text")),
          GridItem(xs = 12)(
            Typography(color = Typography.Colors.Secondary)("Secondary color text")),
          GridItem(xs = 12)(Typography(color = Typography.Colors.Error)("Error color text")),
          GridItem(xs = 12)(Button()("Flat button")),
          GridItem(xs = 12)(Button(color = Button.Colors.Primary)("Flat primary button")),
          GridItem(xs = 12)(Button(color = Button.Colors.Secondary)("Flat secondary button")),
          GridItem(xs = 12)(Button(variant = Button.Variants.Raised)("Raised button")),
          GridItem(xs = 12)(Button(variant = Button.Variants.Raised,
                                   color = Button.Colors.Primary)("Raised primary button")),
          GridItem(xs = 12)(Button(variant = Button.Variants.Raised,
                                   color = Button.Colors.Secondary)("Raised secondary button")),
          GridItem(xs = 12)(Button(variant = Button.Variants.Floating)("+")),
          GridItem(xs = 12)(TextField(
            autoFocus = false,
            label = Typography()("Default text field"),
            placeholder = "Placeholder text",
            value = "Current value",
            helperText = Typography(component = "span")("Helper text")
          )()),
          GridItem(xs = 12)(TextField(
            autoFocus = false,
            error = true,
            label = Typography()("Error text field"),
            placeholder = "Error placeholder text",
            value = "Current value",
            helperText = Typography(component = "span")("Error helper text")
          )()),
          GridItem(xs = 12)(TextField(
            autoFocus = false,
            disabled = true,
            label = Typography()("Disabled text field"),
            placeholder = "Disabled placeholder text",
            value = "Current value",
            helperText = Typography(component = "span")("Disabled helper text")
          )()),
          Paper(style = Styles.paperContainer)(
            Typography(variant = Typography.Variants.SubHeading)("Paper"),
            Typography()("This is a paper container.")
          ),
          GridItem(xs = 12)()
        ))
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .build

  def apply() = component.withProps(Props())
}
