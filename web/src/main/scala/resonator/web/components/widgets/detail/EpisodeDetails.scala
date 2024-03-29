package resonator.web.components.widgets.detail

import resonator.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.web.CssSettings._
import resonator.web.SharedStyles
import resonator.web.audio.AudioPlayer
import resonator.web.components.ComponentHelpers._
import resonator.web.components.materialui._
import resonator.web.components.widgets.card.EpisodeCard
import resonator.web.components.widgets.feed.DotableActionsCardWrapper
import resonator.web.utils.BaseBackend
import resonator.web.utils.Debounce
import org.scalajs.dom
import scalacss.internal.mutable.StyleSheet

import scala.scalajs.js

object EpisodeDetails {

  object Styles extends StyleSheet.Inline {
    import dsl._

  }

  case class Props(dotable: Dotable)
  case class State(breakpoint: String)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val playAudio = bs.props map { p =>
      AudioPlayer.startPlayingEpisode(p.dotable)
    }

    val resizeListener: js.Function1[js.Dynamic, Unit] = Debounce.debounce1(waitMs = 200) {
      (e: js.Dynamic) =>
        bs.modState(_.copy(breakpoint = currentBreakpointString)).runNow()
    }
    dom.window.addEventListener("resize", resizeListener)

    val handleWillUnmount: Callback = Callback {
      dom.window.removeEventListener("resize", resizeListener)
    }

    def render(p: Props, s: State): VdomElement = {
      val title = p.dotable.getCommon.title.trim
      val description = p.dotable.getCommon.description.trim

      val shouldInline = s.breakpoint match {
        case "xs" => false
        case "sm" => false
        case _ => true
      }

      GridContainer(spacing = 8)(
        GridItem(xs = 12, sm = 6, lg = 4)(
          DotableActionsCardWrapper(p.dotable, alwaysExpanded = true)(
            EpisodeCard(dotable = p.dotable, showTitle = false)())
        ),
        GridItem(xs = 12, sm = 6, lg = 8)(
          Typography(variant = Typography.Variants.SubHeading)(<.b(title)),
          Typography(variant = Typography.Variants.Body1,
                     dangerouslySetInnerHTML = linkifyAndSanitize(description))()
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State(breakpoint = currentBreakpointString))
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, state))
    .componentWillUnmount(x => x.backend.handleWillUnmount)
    .build

  def apply(p: Props) = component.withKey(p.dotable.id).withProps(p)
}
