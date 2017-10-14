package kurtome.components.widgets

import dote.proto.model.doteentity.DoteEntity
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.Styles
import kurtome.components.materialui._

object EntityDetails {

  class Backend(bs: BackendScope[DoteEntity, DoteEntity]) {
    def render(props: DoteEntity, s: DoteEntity): VdomElement =
      Paper(className = Styles.detailsRoot)(
        Grid(container = true)(
          Grid(item = true, xs = 4)(
            Typography(className = Styles.detailsTitle, typographyType = Typography.Type.Title)(
              s.common.get.title)
          ),
          Grid(item = true, xs = 4)(
            EntityTile.component(EntityTile.Props(s, size = "250px"))
          ),
          Grid(item = true, xs = 4)(),
          Grid(item = true, xs = 12)(
            Typography(typographyType = Typography.Type.Body1)(s.common.get.descriptionHtml)
          )
        )
      )
  }

  val component = ScalaComponent
    .builder[DoteEntity](this.getClass.getSimpleName)
    .initialState(DoteEntity())
    .backend(new Backend(_))
    .renderPS((builder, props, state) => builder.backend.render(props, props))
    .build
}
