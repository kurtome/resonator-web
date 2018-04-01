package kurtome.dote.web.components.materialui

import scala.scalajs.js.annotation.JSImport
import japgolly.scalajs.react._

import scala.scalajs.js

/**
  * Scala accessors for icon components
  */
object Icons {

  @js.native
  trait IconProps extends js.Object {
    var style: js.UndefOr[js.Dynamic] = js.native
  }

  @JSImport("mdi-material-ui/Home", JSImport.Default)
  @js.native
  private object HomeRaw extends js.Object {}
  val Home = JsComponent[Null, Children.None, Null](HomeRaw)

  @JSImport("mdi-material-ui/Plus", JSImport.Default)
  @js.native
  private object AddRaw extends js.Object {}
  val Add = JsComponent[Null, Children.None, Null](AddRaw)

  @JSImport("mdi-material-ui/Stop", JSImport.Default)
  @js.native
  private object StopRaw extends js.Object {}
  val Stop = JsComponent[Null, Children.None, Null](StopRaw)

  @JSImport("mdi-material-ui/Pause", JSImport.Default)
  @js.native
  private object PauseRaw extends js.Object {}
  val Pause = JsComponent[Null, Children.None, Null](PauseRaw)

  @JSImport("mdi-material-ui/Play", JSImport.Default)
  @js.native
  private object PlayArrowRaw extends js.Object {}
  val PlayArrow = JsComponent[Null, Children.None, Null](PlayArrowRaw)

  @JSImport("mdi-material-ui/AccountCircle", JSImport.Default)
  @js.native
  private object AccountCircleRaw extends js.Object {}
  val AccountCircle = JsComponent[Null, Children.None, Null](AccountCircleRaw)

  @JSImport("mdi-material-ui/Close", JSImport.Default)
  @js.native
  private object CloseRaw extends js.Object {}
  val Close = JsComponent[Null, Children.None, Null](CloseRaw)

  @JSImport("material-ui-icons/Forward30", JSImport.Default)
  @js.native
  private object Forward30Raw extends js.Object {}
  val Forward30 = JsComponent[Null, Children.None, Null](Forward30Raw)

  @JSImport("material-ui-icons/Replay10", JSImport.Default)
  @js.native
  private object Replay10Raw extends js.Object {}
  val Replay10 = JsComponent[Null, Children.None, Null](Replay10Raw)

  @JSImport("mdi-material-ui/Magnify", JSImport.Default)
  @js.native
  private object SearchRaw extends js.Object {}
  val Search = JsComponent[Null, Children.None, Null](SearchRaw)

  @JSImport("mdi-material-ui/ArrowLeft", JSImport.Default)
  @js.native
  private object KeyboardArrowLeftRaw extends js.Object {}
  val KeyboardArrowLeft = JsComponent[Null, Children.None, Null](KeyboardArrowLeftRaw)

  @JSImport("mdi-material-ui/ArrowRight", JSImport.Default)
  @js.native
  private object KeyboardArrowRightRaw extends js.Object {}
  val KeyboardArrowRight = JsComponent[Null, Children.None, Null](KeyboardArrowRightRaw)

  @JSImport("mdi-material-ui/ChevronDown", JSImport.Default)
  @js.native
  private object ExpandMoreRaw extends js.Object {}
  val ChevronDown = JsComponent[Null, Children.None, Null](ExpandMoreRaw)

  @JSImport("mdi-material-ui/ChevronUp", JSImport.Default)
  @js.native
  private object ExpandLessRaw extends js.Object {}
  val ChevronUp = JsComponent[Null, Children.None, Null](ExpandLessRaw)

  @JSImport("mdi-material-ui/ChevronLeft", JSImport.Default)
  @js.native
  private object ChevronLeftRaw extends js.Object {}
  private val ChevronLeftComponent = JsComponent[IconProps, Children.None, Null](ChevronLeftRaw)
  def ChevronLeft(style: js.UndefOr[js.Dynamic] = js.undefined) = {
    ChevronLeftComponent.withProps(props(style))()
  }

  @JSImport("mdi-material-ui/ChevronRight", JSImport.Default)
  @js.native
  private object ChevronRightRaw extends js.Object {}
  private val ChevronRightComponent = JsComponent[IconProps, Children.None, Null](ChevronRightRaw)
  def ChevronRight(style: js.UndefOr[js.Dynamic] = js.undefined) = {
    ChevronRightComponent.withProps(props(style))()
  }

  private def props(style: js.UndefOr[js.Dynamic]): IconProps = {
    val props = new js.Object().asInstanceOf[IconProps]
    props.style = style
    props
  }

}
