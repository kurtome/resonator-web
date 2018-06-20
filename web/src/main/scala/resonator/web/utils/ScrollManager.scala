package resonator.web.utils

import resonator.shared.util.observer.Observable
import resonator.shared.util.observer.SimpleObservable
import org.scalajs.dom
import wvlet.log.LogSupport

import scala.scalajs.js

object ScrollManager extends LogSupport {

  case class ScrollInfo(x: Double, y: Double)

  var current: ScrollInfo = ScrollInfo(dom.window.pageXOffset, dom.window.pageYOffset)
  val stateObservable: Observable[ScrollInfo] = SimpleObservable()

  val scrollListener = Debounce.debounce1(200)((e: js.Dynamic) => {
    current = ScrollInfo(dom.window.pageXOffset, dom.window.pageYOffset)
    stateObservable.notifyObservers(current)
  })

  dom.window.addEventListener("scroll", scrollListener)

}
