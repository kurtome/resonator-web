package kurtome.dote.web.constants

import kurtome.dote.shared.util.observer.Observable
import kurtome.dote.shared.util.observer.SimpleObservable
import kurtome.dote.web.components.materialui.{Colors, MuiThemeProvider}
import wvlet.log.LogSupport

import scala.scalajs._
import scala.scalajs.js.JSON

object MuiTheme extends LogSupport {

  val isDayTime = {
    val time = new js.Date()
    val hours = time.getHours()
    debug(hours)
    hours >= 7 && hours < 19
  }

  var curTheme: js.Dynamic =
    createTheme(
      light = isDayTime,
      primary = PaletteColor(light = Colors.cyan.`300`,
                             main = Colors.cyan.`500`,
                             dark = Colors.cyan.`700`,
                             contrastText = Colors.grey.`900`),
      secondary = PaletteColor(light = Colors.teal.`100`,
                               main = Colors.teal.`200`,
                               dark = Colors.teal.`300`,
                               contrastText = Colors.grey.`900`)
    )

  def theme: js.Dynamic = curTheme

  def primaryTextColor: String = theme.palette.text.primary.asInstanceOf[String]
  def secondaryTextColor: String = theme.palette.text.secondary.asInstanceOf[String]

  def set(theme: js.Dynamic) = {
    curTheme = theme
    stateObservable.notifyObservers(curTheme)
  }

  val stateObservable: Observable[js.Dynamic] = SimpleObservable()

  private val l$ = js.Dynamic.literal

  @js.native
  trait PaletteColor extends js.Object {
    var light: String = js.native
    var main: String = js.native
    var dark: String = js.native
    var contrastText: String = js.native
  }
  object PaletteColor {
    def apply(light: String, main: String, dark: String, contrastText: String): PaletteColor = {
      val color = new js.Object().asInstanceOf[PaletteColor]
      color.light = light
      color.main = main
      color.dark = dark
      color.contrastText = contrastText
      color
    }
  }

  // Look at the documentation at https://material-ui-next.com/customization/themes/ for
  // that customization is possible.
  def createTheme(light: Boolean, primary: PaletteColor, secondary: PaletteColor): js.Dynamic =
    MuiThemeProvider.CreateMuiTheme.createMuiTheme(
      l$(
        "palette" -> l$(
          "primary" -> primary,
          "secondary" -> secondary,
          "type" -> (if (light) "light" else "dark"),
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
            "backgroundColor" -> (if (light) "#e0e0e0" else "#212121")
          )
          )
        )
      )
    )
}
