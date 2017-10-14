package kurtome.components.widgets

import dote.proto.model.doteentity.DoteEntity
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.Styles
import kurtome.components.materialui._

object EntityTile {

  case class State(entity: DoteEntity)

  class Backend(bs: BackendScope[DoteEntity, State]) {
    def render(s: State): VdomElement =
      Grid(container = true)(
        Grid(item = true)(
          Paper(className = Styles.tileContainer, elevation = 8)(
            <.img(^.className := Styles.nestedImg.className.value,
                  ^.src := s.entity.common.get.imageUrl,
                  ^.width := "175px",
                  ^.height := "175px")
          )
        )
      )
  }

  val component = ScalaComponent
    .builder[DoteEntity](this.getClass.getSimpleName)
    .initialState(State(DoteEntity()))
    .backend(new Backend(_))
    .renderP((b, e) => b.backend.render(State(e)))
    .build

}
