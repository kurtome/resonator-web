package kurtome.dote.web.components.materialui

import scala.scalajs.js.annotation.JSImport
import japgolly.scalajs.react._

import scala.scalajs.js

/**
  * Scala accessors for icon components
  */
object Icons {

  @JSImport("material-ui-icons/Home.js", JSImport.Default)
  @js.native
  private object HomeRaw extends js.Object {}
  val Home = JsComponent[Null, Children.None, Null](HomeRaw)

  @JSImport("material-ui-icons/Add.js", JSImport.Default)
  @js.native
  private object AddRaw extends js.Object {}
  val Add = JsComponent[Null, Children.None, Null](AddRaw)

  @JSImport("material-ui-icons/AccountCircle.js", JSImport.Default)
  @js.native
  private object AccountCircleRaw extends js.Object {}
  val AccountCircle = JsComponent[Null, Children.None, Null](AccountCircleRaw)

}
