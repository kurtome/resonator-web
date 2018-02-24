package kurtome.dote.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.proto.api.feed.{Feed => ApiFeed}
import kurtome.dote.web.components.lib.LazyLoad
import kurtome.dote.web.components.materialui.CircularProgress
import kurtome.dote.web.components.materialui.Grid
import kurtome.dote.web.components.materialui.GridContainer
import kurtome.dote.web.components.materialui.GridItem
import kurtome.dote.web.utils.BaseBackend
import wvlet.log.LogSupport

object Feed extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

    val feedItemContainer = style(
      marginBottom(24 px)
    )
  }

  case class Props(feed: ApiFeed)

  class Backend(val bs: BackendScope[Props, Unit]) extends BaseBackend(Styles) {

    def render(p: Props): VdomElement = {

      if (p.feed.items.isEmpty) {
        GridContainer(justify = Grid.Justify.Center)(
          GridItem()(
            CircularProgress(variant = CircularProgress.Variant.Indeterminate, size = 60f)()
          )
        )
      } else {
        <.div(
          p.feed.items.zipWithIndex map {
            case (item, i) =>
              <.div(
                ^.key := s"$i${item.getDotableList.getList.title}",
                ^.className := Styles.feedItemContainer,
                item.kind match {
                  case FeedItem.Kind.DOTABLE_LIST =>
                    LazyLoad(once = true,
                             height = 200,
                             key = Some(s"$i${item.getDotableList.getList.title}"))(
                      FeedDotableList(item.getDotableList, key = Some(i.toString))()
                    )
                  case _ => {
                    warn("unexpected kind")
                    <.div(^.key := i)
                  }
                }
              )
          } toVdomArray
        )
      }
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .backend(new Backend(_))
    .renderP((builder, props) => builder.backend.render(props))
    .build

  def apply(feed: ApiFeed) = component.withProps(Props(feed))
}
