package kurtome.dote.web.components.widgets

import dote.proto.api.dotable.Dotable
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.web.Styles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.ComponentHelpers._

object EntityTile {

  case class Props(dotable: Dotable, size: String = "175px")

  class Backend(bs: BackendScope[Props, Dotable]) {
    def render(props: Props, s: Dotable): VdomElement =
      Paper(elevation = 8, className = Styles.inlineBlock)(
        <.img(^.className := Styles.nestedImg.className.value,
              ^.src := s.getDetails.getPodcast.imageUrl,
              ^.width := props.size,
              ^.height := props.size)
      )
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(Dotable.defaultInstance)
    .backend(new Backend(_))
    .renderP((builder, props) => builder.backend.render(props, props.dotable))
    .build
}
