package kurtome.dote.web.constants

import kurtome.dote.shared.util.observer.Observable
import kurtome.dote.shared.util.observer.SimpleObservable
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.materialui.{Colors, MuiThemeProvider}
import wvlet.log.LogSupport

import scala.scalajs._
import scala.scalajs.js.annotation.JSName

object MuiTheme extends LogSupport {

  val isDayTime = {
    val time = new js.Date()
    val hours = time.getHours()
    hours >= 7 && hours < 19
  }

//  var curTheme: Theme =
//    createTheme(
//      light = isDayTime,
//      primary = Left(PaletteColor(light = Colors.cyan.`300`,
//                             main = Colors.cyan.`500`,
//                             dark = Colors.cyan.`700`,
//                             contrastText = Colors.grey.`900`)),
//      secondary = Left(PaletteColor(light = Colors.teal.`100`,
//                               main = Colors.teal.`200`,
//                               dark = Colors.teal.`300`,
//                               contrastText = Colors.grey.`900`))
//    )

  val lightBackground = PaletteBackground("#fff", "#f9f7e0")
  val darkBackground = PaletteBackground("#424242", "#303030")

  val lightBottomNav = "#f2f2c9"
  val darkBottomNav = "#424242"


  var curTheme: Theme = {
    val isLightTheme = isDayTime
    createTheme(
      light = isLightTheme,
      primary = PaletteColor("#56b8af"),
      secondary = PaletteColor("#aa2f2c"),
    )
  }

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
          "background" -> (if (light) lightBackground else darkBackground)
        ),
        "typography" -> l$(
          "fontFamily" -> "syntesia",
          "htmlFontSize" -> 14,
          "headline" -> l$(
            "fontFamily" -> "aileronHeavy",
            "textTransform" -> "uppercase"
          ),
          "title" -> l$(
            "fontFamily" -> "aileronHeavy",
            "textTransform" -> "uppercase"
          ),
          "subheading" -> l$(
            "fontFamily" -> "aileronSemiBold",
          ),
          "button" -> l$(
            "fontFamily" -> "aileronRegular",
            "textTransform" -> "uppercase"
          )
        ),
        "overrides" -> l$(
          "MuiLinearProgress" -> l$(
            "primaryColor" -> l$(
              "backgroundColor" -> "transparent"
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
