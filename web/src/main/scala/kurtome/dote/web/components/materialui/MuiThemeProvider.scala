package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._
import kurtome.dote.web.constants.MuiTheme

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

/**
  * Wrapper for https://material-ui.com/api/mui-theme-provider/
  *
  * <p>See more details for usage at https://material-ui.com/customization/themes/
  */
object MuiThemeProvider {

  @JSImport("@material-ui/core/styles/MuiThemeProvider", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  @JSImport("@material-ui/core/styles/createMuiTheme", JSImport.Namespace)
  @js.native
  object CreateMuiTheme extends js.Object {
    @JSName("default")
    def createMuiTheme(themeOverrides: js.Dynamic): MuiTheme.Theme = js.native
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var disableStylesGeneration: js.UndefOr[Boolean] = js.native
    var theme: MuiTheme.Theme = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(theme: MuiTheme.Theme, disableStylesGeneration: js.UndefOr[Boolean] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.disableStylesGeneration = disableStylesGeneration
    p.theme = theme
    component.withProps(p)
  }
}
