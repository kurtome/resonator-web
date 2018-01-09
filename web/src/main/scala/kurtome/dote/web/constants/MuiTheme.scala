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
        "primary" -> Colors.grey,
        "secondary" -> Colors.blue,
        "type" -> "light",
        "shades" -> l$(
          "light" -> l$(
            "background" -> l$(
              "default" -> "#616161",
              "paper" -> "#eeeeee",
            )
          )
        ),
        "background" -> l$(
          "default" -> "#f5f5f5",
          "paper" -> "#e0e0e0",
        )
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
        )
      )
    )
  )
}
