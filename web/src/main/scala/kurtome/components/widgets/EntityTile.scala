package kurtome.components.widgets

import dote.proto.model.dote_entity.DoteEntity
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.Styles
import kurtome.components.materialui._
import kurtome.components.ComponentHelpers._

object EntityTile {

  case class Props(entity: DoteEntity, size: String = "175px")

  class Backend(bs: BackendScope[Props, DoteEntity]) {
    def render(props: Props, s: DoteEntity): VdomElement =
      Paper(elevation = 8, className = Styles.inlineBlock)(
        <.img(^.className := Styles.nestedImg.className.value,
              ^.src := s.common.get.imageUrl,
              ^.width := props.size,
              ^.height := props.size)
      )
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(DoteEntity())
    .backend(new Backend(_))
    .renderP((builder, props) => builder.backend.render(props, props.entity))
    .build
}
