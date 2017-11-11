package kurtome.dote.web.constants

import kurtome.dote.web.components.materialui.{Colors, MuiThemeProvider}

import scala.scalajs._
import scala.scalajs.js.JSON

object MuiTheme {
  private val l$ = js.Dynamic.literal

  // Look at the documentation at https://material-ui-next.com/customization/themes/ for
  // that customization is possible.
  val theme: js.Dynamic = MuiThemeProvider.CreateMuiTheme.createMuiTheme(
    l$(
      "palette" -> l$(
        "primary" -> Colors.blueGrey,
        "secondary" -> Colors.deepOrange,
        "light" -> l$(
          "background" -> l$(
            "default" -> "#e0ddc0",
            "paper" -> "#fff",
            "appBar" -> "#f5f5f5",
            "contentFrame" -> "#eeeeee"
          )
        )
      ),
      "typography" -> l$(
        "fontFamily" -> "syntesia",
        "htmlFontSize" -> 13,
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
          "textTransform" -> "uppercase"
        ),
        "button" -> l$(
          "fontFamily" -> "aileronRegular",
          "textTransform" -> "uppercase"
        )
      )
    )
  )
}
