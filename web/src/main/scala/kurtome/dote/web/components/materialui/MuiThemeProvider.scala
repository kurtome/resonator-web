package kurtome.dote.web.components.materialui

import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

/**
  * Wrapper for https://material-ui-next.com/api/mui-theme-provider/
  *
  * <p>See more details for usage at https://material-ui-next.com/customization/themes/
  */
object MuiThemeProvider {

  @JSImport("material-ui/styles/MuiThemeProvider.js", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  @JSImport("material-ui/styles/createMuiTheme.js", JSImport.Namespace)
  @js.native
  object CreateMuiTheme extends js.Object {
    @JSName("default")
    def createMuiTheme(themeOverrides: js.Dynamic): js.Dynamic = js.native
  }

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var disableStylesGeneration: js.UndefOr[Boolean] = js.native
    var theme: js.Dynamic = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(theme: js.Dynamic, disableStylesGeneration: js.UndefOr[Boolean] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.disableStylesGeneration = disableStylesGeneration
    p.theme = theme
    component.withProps(p)
  }
}
