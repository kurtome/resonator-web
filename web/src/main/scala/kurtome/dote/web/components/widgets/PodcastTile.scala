package kurtome.dote.web.components.widgets

import kurtome.dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.scalajs.js

object PodcastTile extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val wrapper = style(
      position.relative
    )

    val container = style(
      position.absolute,
      pointerEvents := "auto"
    )

  }

  private object Animations extends StyleSheet.Inline {
    import dsl._
    val fadeInImage = keyframes(
      (0 %%) -> keyframe(opacity(0)),
      (100 %%) -> keyframe(opacity(1))
    )
  }
  Animations.addToDocument()

  case class Props(dotable: Dotable, elevation: Int, width: String, disableActions: Boolean)
  case class State(imgLoaded: Boolean = false,
                   hover: Boolean = false,
                   smileCount: Int = 0,
                   cryCount: Int = 0,
                   laughCount: Int = 0,
                   scowlCount: Int = 0)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def render(p: Props, s: State): VdomElement = {
      val id = p.dotable.id
      val slug = p.dotable.slug
      val detailRoute = DetailsRoute(id = id, slug = slug)

      val url = if (p.dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
        p.dotable.getRelatives.getParent.getDetails.getPodcast.imageUrl
      } else {
        p.dotable.getDetails.getPodcast.imageUrl
      }

      Paper(elevation = if (s.hover) p.elevation * 2 else p.elevation,
            className = SharedStyles.inlineBlock)(
        <.div(
          ^.className := Styles.wrapper,
          ^.width := p.width,
          ^.height := p.width,
          ^.onMouseEnter --> bs.modState(_.copy(hover = true)),
          ^.onMouseLeave --> bs.modState(_.copy(hover = false)),
          doteRouterCtl.link(detailRoute)(
            ^.className := Styles.container,
            EntityImage(dotable = p.dotable, width = p.width)()
          ),
          if (p.disableActions) {
            <.div()
          } else {
            TileActionShim(p.dotable, s.hover)()
          }
        )
      )

    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialStateFromProps(p => {
      val dote = p.dotable.getDote
      State(smileCount = dote.smileCount,
            cryCount = dote.cryCount,
            laughCount = dote.laughCount,
            scowlCount = dote.scowlCount)
    })
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable,
            elevation: Int = 6,
            width: String = "175px",
            disableActions: Boolean = false) =
    component.withProps(Props(dotable, elevation, width, disableActions))
}
