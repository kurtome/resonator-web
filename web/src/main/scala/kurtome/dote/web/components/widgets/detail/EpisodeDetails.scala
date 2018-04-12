package kurtome.dote.web.components.widgets.detail

import kurtome.dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.audio.AudioPlayer
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets._
import kurtome.dote.web.components.widgets.button.ShareButton
import kurtome.dote.web.components.widgets.button.emote.DoteEmoteButton
import kurtome.dote.web.components.widgets.card.EpisodeCard
import kurtome.dote.web.components.widgets.feed.DotableActionsCardWrapper
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.Debounce
import kurtome.dote.web.utils.GlobalLoadingManager
import org.scalajs.dom
import scalacss.internal.mutable.StyleSheet

import scala.scalajs.js

object EpisodeDetails {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val descriptionText = style(
      marginTop(SharedStyles.spacingUnit)
    )
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
      val description = p.dotable.getCommon.description

      val shouldInline = s.breakpoint match {
        case "xs" => false
        case "sm" => false
        case _ => true
      }

      if (shouldInline) {
        <.div(
          <.div(
            ^.position := "relative",
            ^.marginRight := "8px",
            ^.marginBottom := "8px",
            ^.float.left,
            ^.width := "500px",
            DotableActionsCardWrapper(p.dotable, alwaysExpanded = true)(
              EpisodeCard(dotable = p.dotable)())
          ),
          Typography(component = "span",
                     style = Styles.descriptionText,
                     variant = Typography.Variants.Body1,
                     dangerouslySetInnerHTML = linkifyAndSanitize(description))()
        )
      } else {
        GridContainer(spacing = 0)(
          GridItem(xs = 12)(
            <.div(
              ^.position := "relative",
              ^.marginBottom := "8px",
              ^.float.left,
              ^.width := "100%",
              DotableActionsCardWrapper(p.dotable, alwaysExpanded = true)(
                EpisodeCard(dotable = p.dotable)())
            )
          ),
          GridItem(xs = 12)(
            Typography(component = "span",
                       style = Styles.descriptionText,
                       variant = Typography.Variants.Body1,
                       dangerouslySetInnerHTML = linkifyAndSanitize(description))()
          )
        )

      }
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
