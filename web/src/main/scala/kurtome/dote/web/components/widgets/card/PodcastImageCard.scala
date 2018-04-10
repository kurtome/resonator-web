package kurtome.dote.web.components.widgets.card

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.constants.MuiTheme
import kurtome.dote.web.utils._
import wvlet.log.LogSupport

import scala.scalajs.js

object PodcastImageCard extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val nestedImg = style(
      position.absolute,
      animation := s"${Animations.fadeInImage.name.value} 1s",
      width(100 %%)
    )

    val placeholder = style(
      backgroundColor :=! MuiTheme.theme.palette.background.paper,
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
  case class State(imgLoaded: Boolean = false, hover: Boolean = false)

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

      <.div(
        ^.position.relative,
        ^.width := p.width,
        if (url.nonEmpty) {
          <.img(
            ^.className := Styles.nestedImg,
            ^.visibility := (if (s.imgLoaded) "visible" else "hidden"),
            ^.onLoad --> bs.modState(_.copy(imgLoaded = true)),
            ^.src := url
          )
        } else {
          <.div(^.position := "absolute")
        },
        // placeholder div while loading, to fill the space
        <.div(
          ^.className := Styles.placeholder
        )
      )

    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderPS((builder, p, s) => builder.backend.render(p, s))
    .build

  def apply(dotable: Dotable, width: String = "175px") =
    component.withProps(Props(dotable, width))
}
