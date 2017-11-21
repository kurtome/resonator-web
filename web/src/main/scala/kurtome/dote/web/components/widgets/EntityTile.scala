package kurtome.dote.web.components.widgets

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.raw.SyntheticEvent
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.DoteRoutes.{DoteRouterCtl, PodcastRoute}
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.utils.MuiInlineStyleSheet

import scala.scalajs.js

object EntityTile {

  private object Styles extends StyleSheet.Inline {
    import dsl._

    val fadeIn = keyframes(
      (0 %%) -> keyframe(opacity(0)),
      (100 %%) -> keyframe(opacity(1))
    )

    val nestedImg = style(
      position.absolute,
      animation := s"${fadeIn.name.value} 1s",
      display.block,
      margin.auto // margin: 'auto' make this img centered in its space
    )

    val placeholder = style(
      backgroundColor(rgb(200, 200, 200))
    )
  }
  Styles.addToDocument()

  case class Props(routerCtl: DoteRouterCtl, dotable: Dotable, size: String = "175px")
  case class State(imgLoaded: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement = {
      val id = p.dotable.id
      val slug = p.dotable.slug
      val detailRoute = PodcastRoute(id = id, slug = slug)

      Paper(elevation = 8, className = SharedStyles.inlineBlock)(
        p.routerCtl.link(detailRoute)(
          <.img(
            ^.className := Styles.nestedImg,
            ^.visibility := (if (s.imgLoaded) "visible" else "hidden"),
            ^.src := p.dotable.getDetails.getPodcast.imageUrl,
            ^.onLoad --> bs.modState(_.copy(imgLoaded = true)),
            ^.width := p.size,
            ^.height := p.size
          ),
          // placeholder div while loading, to fill the space
          <.div(
            ^.className := Styles.placeholder,
            ^.width := p.size,
            ^.height := p.size
          )
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

  def apply(routerCtl: DoteRouterCtl, dotable: Dotable, size: String = "175px") =
    component.withKey(dotable.id).withProps(Props(routerCtl, dotable, size))
}
