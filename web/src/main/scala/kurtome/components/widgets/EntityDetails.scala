package kurtome.components.widgets

import dote.proto.model.doteentity.DoteEntity
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.Styles
import kurtome.components.materialui._

object EntityDetails {

  case class State(entity: DoteEntity)

  class Backend(bs: BackendScope[DoteEntity, State]) {
    def render(s: State): VdomElement =
      Paper(className = Styles.detailsContainer)(
        Grid(container = true)(
          Grid(item = true, xs = 4)(
            Typography(className = Styles.detailsTitle, typographyType = Typography.Type.Title)(
              s.entity.common.get.title)
          ),
          Grid(item = true, xs = 4)(
            <.img(^.className := Styles.nestedImg.className.value,
                  ^.src := s.entity.common.get.imageUrl,
                  ^.width := "175px",
                  ^.height := "175px",
                  ^.alignSelf := "middle")
          ),
          Grid(item = true, xs = 4)(),
          Grid(item = true, xs = 12)(
            Typography(typographyType = Typography.Type.Body1)(s.entity.common.get.descriptionHtml)
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
