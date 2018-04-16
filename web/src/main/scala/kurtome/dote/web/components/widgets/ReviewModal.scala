package kurtome.dote.web.components.widgets

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.materialui.Dialog
import kurtome.dote.web.utils.BaseBackend
import wvlet.log.LogSupport

import scala.language.postfixOps

object ReviewModal extends LogSupport {

  object Styles extends StyleSheet.Inline {

    import dsl._

  }

  object Variants extends Enumeration {
    val Accent = Value // recent activity feed
    val CardHeader = Value // recent activity feed
    val Default = Value
  }
  type Variant = Variants.Value

  case class Props(dotable: Dotable, open: Boolean)
  case class State(editedDote: Dote = Dote.defaultInstance)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {
    def render(p: Props, s: State): VdomElement = {
      Dialog()("Review")
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable, open: Boolean) =
    component.withProps(Props(dotable, open))

}
