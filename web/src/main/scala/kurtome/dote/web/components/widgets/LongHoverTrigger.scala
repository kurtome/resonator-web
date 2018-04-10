package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom

object LongHoverTrigger {

  object HoverStates extends Enumeration {
    val None = Value
    val Entering = Value
    val Triggered = Value
    val Exiting = Value
  }
  type HoverState = HoverStates.Value

  case class Props(onTrigger: Callback, onFinish: Callback, triggerLengthMs: Int)
  case class State(hoverState: HoverState = HoverStates.None)

  class Backend(bs: BackendScope[Props, State]) {

    private var triggerTimeout: Option[Int] = None
    private var finishTimeout: Option[Int] = None

    def onMouseEnter(p: Props, s: State) = Callback {
      s.hoverState match {
        case HoverStates.None => {
          bs.modState(_.copy(hoverState = HoverStates.Entering)).runNow()
          var triggerTimeout = dom.window.setTimeout(() => trigger(p), p.triggerLengthMs)
        }
        case HoverStates.Exiting => {
          finishTimeout.foreach(id => dom.window.clearTimeout(id))
          bs.modState(_.copy(hoverState = HoverStates.Triggered)).runNow()
        }
        case _ => Unit
      }
    }

    def onMouseLeave(p: Props, s: State) = Callback {
      s.hoverState match {
        case HoverStates.Triggered => {
          bs.modState(_.copy(hoverState = HoverStates.Exiting)).runNow()
          var finishTimeout = dom.window.setTimeout(() => finish(p), p.triggerLengthMs)
        }
        case HoverStates.Entering => {
          triggerTimeout.foreach(id => dom.window.clearTimeout(id))
          bs.modState(_.copy(hoverState = HoverStates.None)).runNow()
        }
        case _ => Unit
      }
    }

    val handleUnmount = Callback {
      triggerTimeout.foreach(id => dom.window.clearTimeout(id))
      finishTimeout.foreach(id => dom.window.clearTimeout(id))
    }

    private def trigger(p: Props) = {
      p.onTrigger.runNow()
      bs.modState(_.copy(hoverState = HoverStates.Triggered)).runNow()
    }

    private def finish(p: Props) = {
      p.onFinish.runNow()
      bs.modState(_.copy(hoverState = HoverStates.None)).runNow()
    }

    def render(p: Props, pc: PropsChildren, s: State): VdomElement = {
      <.div(
        ^.pointerEvents.auto,
        ^.onMouseDown --> onMouseEnter(p, s),
        ^.onMouseEnter --> onMouseEnter(p, s),
        ^.onMouseLeave --> onMouseLeave(p, s),
        ^.onMouseUp --> onMouseLeave(p, s),
        pc
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPCS((builder, p, pc, s) => builder.backend.render(p, pc, s))
    .componentWillUnmount(x => x.backend.handleUnmount)
    .build

  def apply(onTrigger: Callback = Callback.empty,
            onFinish: Callback = Callback.empty,
            triggerLengthMs: Int = 1000) =
    component.withProps(Props(onTrigger, onFinish, triggerLengthMs))
}
