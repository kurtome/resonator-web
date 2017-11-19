package kurtome.dote.web.components.widgets

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.InlineStyles
import kurtome.dote.web.DoteRoutes.{DoteRouterCtl, PodcastRoute}
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._

object EntityTile {

  case class Props(routerCtl: DoteRouterCtl, dotable: Dotable, size: String = "175px")

  class Backend(bs: BackendScope[Props, Dotable]) {
    def render(props: Props, s: Dotable): VdomElement = {
      val id = s.id
      val slug = s.slug
      val detailRoute = PodcastRoute(id = id, slug = slug)

      Paper(elevation = 8, className = InlineStyles.inlineBlock)(
        props.routerCtl.link(detailRoute)(
          <.img(^.className := InlineStyles.nestedImg.className.value,
                ^.src := s.getDetails.getPodcast.imageUrl,
                ^.width := props.size,
                ^.height := props.size)
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(Dotable.defaultInstance)
    .backend(new Backend(_))
    .renderP((builder, props) => builder.backend.render(props, props.dotable))
    .build

  def apply(p: Props) = component.withKey(p.dotable.id).withProps(p)
}
