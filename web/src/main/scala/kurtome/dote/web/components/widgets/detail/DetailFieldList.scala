package kurtome.dote.web.components.widgets.detail

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.VdomElement
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.SharedStyles
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.utils.BaseBackend

import scalacss.ScalaCssReact._

object DetailFieldList {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val detailLabel = style(
      marginRight(SharedStyles.spacingUnit)
    )
  }

  case class Props(detailFields: Seq[DetailField])
  case class DetailField(label: String, values: DetailFieldValue*)

  sealed trait DetailFieldValue {
    val text: String
  }
  case class TextFieldValue(text: String) extends DetailFieldValue
  case class LinkFieldValue(text: String, url: String) extends DetailFieldValue

  class Backend(bs: BackendScope[Props, Unit]) extends BaseBackend(Styles) {

    def render(p: Props): VdomElement = {
      Grid(container = true, spacing = 0, alignItems = Grid.AlignItems.FlexStart)(
        p.detailFields flatMap { detailField =>
          val label = <.b(Styles.detailLabel, detailField.label)
          val contents: Seq[VdomElement] =
            detailField.values map { value =>
              val valueContent: VdomElement = value match {
                case link: LinkFieldValue =>
                  <.a(^.href := link.url, ^.target := "_blank", link.text)
                case _ => <.span(value.text)
              }
              val elem: VdomElement = Grid(
                key = Some(detailField.label + "value"),
                item = true,
                xs = 10)(Typography(variant = Typography.Variants.Caption)(valueContent))
              elem
            }

          Seq[VdomElement](
            Grid(key = Some(detailField.label + "label"), item = true, xs = 2)(
              Typography(variant = Typography.Variants.Caption)(label))) ++ contents
        } toVdomArray
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderP((builder, props) => builder.backend.render(props))
    .build

  def apply(detailFields: Seq[DetailField]) = component.withProps(Props(detailFields))

}
