package resonator.web.components.lib

import japgolly.scalajs.react
import japgolly.scalajs.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSImport, JSName}

/**
  * Wrapper for https://github.com/jasonslyvia/react-lazyload
  */
object LazyLoad {

  @JSImport("react-lazyload", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  // NOTE: not all props exposed
  @js.native
  trait Props extends js.Object {
    var height: js.UndefOr[Int] = js.native
    var once: js.UndefOr[Boolean] = js.native
    var offset: js.UndefOr[Int] = js.native
    var scroll: js.UndefOr[Boolean] = js.native
    var resize: js.UndefOr[Boolean] = js.native
    var unmountIfInvisible: js.UndefOr[Boolean] = js.native
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  def apply(key: Option[react.Key] = None,
            height: js.UndefOr[Int] = js.undefined,
            once: js.UndefOr[Boolean] = js.undefined,
            offset: js.UndefOr[Int] = js.undefined,
            scroll: js.UndefOr[Boolean] = js.undefined,
            resize: js.UndefOr[Boolean] = js.undefined,
            unmountIfInvisible: js.UndefOr[Boolean] = js.undefined) = {
    val p = (new js.Object).asInstanceOf[Props]
    p.height = height
    p.once = once
    p.offset = offset
    p.resize = resize
    p.unmountIfInvisible = unmountIfInvisible

    if (key.isDefined) {
      component.withKey(key.get).withProps(p)
    } else {
      component.withProps(p)
    }
  }
}
