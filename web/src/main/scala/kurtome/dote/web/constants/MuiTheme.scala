package kurtome.dote.web.constants

import kurtome.dote.shared.util.observer.Observable
import kurtome.dote.shared.util.observer.SimpleObservable
import kurtome.dote.web.components.materialui.{Colors, MuiThemeProvider}
import wvlet.log.LogSupport

import scala.scalajs._
import scala.scalajs.js.annotation.JSName

object MuiTheme extends LogSupport {

  val isDayTime = {
    val time = new js.Date()
    val hours = time.getHours()
    hours >= 6 && hours < 20
  }

  val lightBackground = PaletteBackground("#fff", "#f9f7e0")
  val darkBackground = PaletteBackground("#424242", "#303030")

  val lightBottomNav = "#f2f2c9"
  val darkBottomNav = "#424242"

  /**
    * Constant dark theme, can be used in sections that are dark all the time.
    */
  val darkTheme = createThemeWithDefaultPalette(false)

  /**
    * Constant light theme, can be used in sections that are light all the time.
    */
  val lightTheme = createThemeWithDefaultPalette(true)

  private def createThemeWithDefaultPalette(isLight: Boolean) = {
    createTheme(
      light = isLight,
      primary = PaletteColor("#25baaf", "#3C807a", "#316d66"),
      secondary = PaletteColor("#aa2f2c")
    )
  }

  var curTheme: Theme = {
    if (isDayTime) {
      lightTheme
    } else {
      // disabling night mode until visual bugs are fixed
      lightTheme
      //darkTheme
    }
  }

  /**
    * The current theme, can be changed on the theme page and by default updates based on the
    * time of day.
    */
  def theme: Theme = curTheme

  def primaryTextColor: String = theme.palette.text.primary.asInstanceOf[String]
  def secondaryTextColor: String = theme.palette.text.secondary.asInstanceOf[String]

  def set(theme: Theme) = {
    curTheme = theme
    stateObservable.notifyObservers(curTheme)
  }

  val stateObservable: Observable[Theme] = SimpleObservable()

  private val l$ = js.Dynamic.literal

  @js.native
  trait PaletteColor extends js.Object {
    var light: String = js.native
    var main: String = js.native
    var dark: String = js.native
    var contrastText: String = js.native
  }
  object PaletteColor {
    def apply(main: String): PaletteColor = {
      val color = new js.Object().asInstanceOf[PaletteColor]
      color.main = main
      color
    }

    def apply(light: String, main: String, dark: String): PaletteColor = {
      val color = new js.Object().asInstanceOf[PaletteColor]
      color.light = light
      color.main = main
      color.dark = dark
      color
    }

    def apply(light: String, main: String, dark: String, contrastText: String): PaletteColor = {
      val color = new js.Object().asInstanceOf[PaletteColor]
      color.light = light
      color.main = main
      color.dark = dark
      color.contrastText = contrastText
      color
    }
  }

  @js.native
  trait PaletteCommon extends js.Object {
    val black: String = js.native
    val white: String = js.native
  }

  @js.native
  trait PaletteText extends js.Object {
    val primary: String = js.native
    val secondary: String = js.native
    val hint: String = js.native
    val disabled: String = js.native
  }

  @js.native
  trait PaletteBackground extends js.Object {
    var paper: String = js.native
    var default: String = js.native
  }

  object PaletteBackground {
    def apply(paper: String, default: String): PaletteBackground = {
      val background = new js.Object().asInstanceOf[PaletteBackground]
      background.paper = paper
      background.default = default
      background
    }
  }

  @js.native
  trait PaletteExtras extends js.Object {
    var cardHeader: String = js.native
    var accentPaperBackground: String = js.native
    var pageButtonFill: String = js.native

  }
  object PaletteExtras {
    def apply(cardHeader: String = "#dce5e3",
              accentPaperBackground: String = "#edf4f3",
              pageButtonFill: String = "#cae0db") = {
      val extras = new js.Object().asInstanceOf[PaletteExtras]
      extras.cardHeader = cardHeader
      extras.accentPaperBackground = accentPaperBackground
      extras.pageButtonFill = pageButtonFill
      extras
    }
  }

  @js.native
  trait PaletteAction extends js.Object {
    val active: String = js.native
    val hover: String = js.native
    val selected: String = js.native
    val disabled: String = js.native
    val disabledBackground: String = js.native
  }

  @js.native
  trait Palette extends js.Object {
    val common: PaletteCommon = js.native
    @JSName("type")
    val paletteType: String = js.native
    val primary: PaletteColor = js.native
    val secondary: PaletteColor = js.native
    val error: PaletteColor = js.native
    val grey: Colors.Color = js.native
    val contrastThreshold: Float = js.native
    val tonalOffset: Float = js.native
    val text: PaletteText = js.native
    val background: PaletteBackground = js.native
    val action: PaletteAction = js.native
    val divider: String = js.native
    val extras: PaletteExtras = js.native
  }

  @js.native
  trait Breakpoints extends js.Object {
    val keys: js.Array[String] = js.native
    val values: js.Dictionary[Int] = js.native
  }

  @js.native
  trait Spacing extends js.Object {
    val unit: Int = js.native
  }

  @js.native
  trait Theme extends js.Object {
    val palette: Palette = js.native
    val typography: js.Dynamic = js.native
    val mixins: js.Dynamic = js.native
    val breakpoints: Breakpoints = js.native
    val shadows: js.Dynamic = js.native
    val transitions: js.Dynamic = js.native
    val spacing: Spacing = js.native
    val zIndex: js.Dynamic = js.native
  }

  // Look at the documentation at https://material-ui-next.com/customization/themes/ for
  // that customization is possible.
  def createTheme(light: Boolean, primary: PaletteColor, secondary: PaletteColor): Theme =
    MuiThemeProvider.CreateMuiTheme.createMuiTheme(
      l$(
        "palette" -> l$(
          "primary" -> primary,
          "secondary" -> secondary,
          "type" -> (if (light) "light" else "dark"),
          "background" -> (if (light) lightBackground else darkBackground),
          "extras" -> PaletteExtras()
        ),
        "typography" -> l$(
          "fontFamily" -> "roboto",
          "htmlFontSize" -> 16,
          "headline" -> l$(
            "fontFamily" -> "roboto",
            "textTransform" -> "uppercase"
          ),
          "title" -> l$(
            "fontFamily" -> "roboto",
            "textTransform" -> "uppercase"
          ),
          "subheading" -> l$(
            "fontFamily" -> "roboto"
          ),
          "button" -> l$(
            "fontFamily" -> "roboto",
            "textTransform" -> js.undefined,
          )
        ),
        "overrides" -> l$(
          "MuiLinearProgress" -> l$(
            "colorPrimary" -> l$(
              "backgroundColor" -> "transparent"
            ),
            "barColorPrimary" -> l$(
              "backgroundColor" -> primary.light
            )
          ),
          "MuiCircularProgress" -> l$(
            "colorPrimary" -> l$(
              "color" -> primary.light
            )
          ),
          "MuiCheckbox" -> l$(
            "checked" -> l$(
              "color" -> primary.light
            ),
            "checkedSecondary" -> l$(
              "color" -> primary.light
            )
          ),
          "MuiButton" -> l$(
            "flatPrimary" -> l$(
              "color" -> primary.light
            )
          ),
          "MuiBottomNavigation" -> l$(
            "root" -> l$(
              "backgroundColor" -> (if (light) lightBottomNav else darkBottomNav)
            )
          ),
          "MuiFormControl" -> l$(
            "root" -> l$(
              "marginTop" -> "8px",
              "marginBottom" -> "8px"
            ),
            "marginNormal" -> l$(
              "marginTop" -> "8px",
              "marginBottom" -> "8px"
            ),
            "marginDense" -> l$(
              "marginTop" -> "4px",
              "marginBottom" -> "4px"
            )
          )
        )
      )
    )
}
