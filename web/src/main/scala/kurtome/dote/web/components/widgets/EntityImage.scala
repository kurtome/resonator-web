package kurtome.dote.web.components.widgets

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.action.set_dote.SetDoteRequest
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.rpc.DoteProtoServer
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.scalajs.js

object EntityImage extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val wrapper = style(
      position.relative
    )

    val container = style(
      position.absolute,
      pointerEvents := "auto"
    )

    val imageContainer = style(
      )

    val overlayContainer = style(
      position.absolute,
      pointerEvents := "none",
      width(100 %%),
      height(100 %%)
    )

    val overlayActionsContainer = style(
      width(100 %%),
      height(100 %%)
    )

    val overlay = style(
      position.absolute,
      backgroundColor(rgba(255, 255, 255, 0.4)),
      width(100 %%),
      height(100 %%)
    )

    val nestedImg = style(
      position.absolute,
      animation := s"${Animations.fadeInImage.name.value} 1s",
      width(100 %%)
    )

    val placeholder = style(
      position.absolute,
      backgroundColor(rgb(200, 200, 200)),
      width(100 %%),
      // Use padding top to force the height of the div to match the width
      paddingTop(100 %%)
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

  case class Props(dotable: Dotable, width: String = "175px")
  case class State(imgLoaded: Boolean = false,
                   hover: Boolean = false,
                   smileCount: Int = 0,
                   cryCount: Int = 0,
                   laughCount: Int = 0,
                   scowlCount: Int = 0)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val sendDoteToServer: js.Function0[Unit] = Debounce.debounce0(waitMs = 2000) { () =>
      val p: Props = bs.props.runNow()
      val s: State = bs.state.runNow()
      val f = DoteProtoServer.setDote(
        SetDoteRequest(p.dotable.id,
                       Some(
                         Dote(smileCount = s.smileCount,
                              laughCount = s.laughCount,
                              cryCount = s.cryCount,
                              scowlCount = s.scowlCount))))
      GlobalLoadingManager.addLoadingFuture(f)
    }

    def render(p: Props, s: State): VdomElement = {
      val id = p.dotable.id
      val slug = p.dotable.slug
      val detailRoute = DetailsRoute(id = id, slug = slug)

      val url = if (p.dotable.kind == Dotable.Kind.PODCAST_EPISODE) {
        p.dotable.getRelatives.getParent.getDetails.getPodcast.imageUrl
      } else {
        p.dotable.getDetails.getPodcast.imageUrl
      }

      <.div(
        ^.className := Styles.wrapper,
        ^.width := p.width,
        ^.height := p.width,
        <.div(
          ^.className := Styles.container,
          ^.width := p.width,
          ^.height := p.width,
          ^.className := Styles.imageContainer,
          // placeholder div while loading, to fill the space
          <.div(
            ^.className := Styles.placeholder
          ),
          if (url.nonEmpty) {
            <.img(
              ^.className := Styles.nestedImg,
              ^.visibility := (if (s.imgLoaded) "visible" else "hidden"),
              ^.onLoad --> bs.modState(_.copy(imgLoaded = true)),
              ^.src := url
            )
          } else {
            <.div()
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

  def apply(dotable: Dotable, width: String = "175px") =
    component.withProps(Props(dotable, width))
}
