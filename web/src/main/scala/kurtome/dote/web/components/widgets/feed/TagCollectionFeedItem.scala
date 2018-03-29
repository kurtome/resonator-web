package kurtome.dote.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.web.CssSettings._
import kurtome.dote.web.DoteRoutes._
import kurtome.dote.web.components.ComponentHelpers._
import kurtome.dote.web.components.materialui._
import kurtome.dote.web.components.widgets.FlatRoundedButton
import kurtome.dote.web.components.widgets.MainContentSection
import kurtome.dote.web.utils.BaseBackend
import kurtome.dote.web.utils.FeedIdRoutes.TagRouteMapper
import scalacss.ScalaCssReact._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global

object TagCollectionFeedItem extends LogSupport {

  object Styles extends StyleSheet.Inline {
    import dsl._

  }
  Styles.addToDocument()

  case class Props(feedItem: FeedItem)
  case class State()

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    def render(p: Props): VdomElement = {
      val collection = p.feedItem.getTagCollection.getTagCollection
      val tags = collection.tags

      MainContentSection(variant = MainContentSection.Variants.Primary)(
        GridContainer(spacing = 8)(
          GridItem(xs = 12)(
            Typography(variant = Typography.Variants.Title)(collection.title)
          ),
          (tags map { tag =>
            GridItem(key = Some(tag.getId.kind.toString + tag.getId.key))(
              FlatRoundedButton(
                variant = FlatRoundedButton.Variants.FillLight,
                onClick = doteRouterCtl.set(TagRouteMapper.toRoute(tag)))(tag.displayValue)
            )
          }) toVdomArray
        )
      )
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .renderP((builder, props) => builder.backend.render(props))
    .build

  def apply(feedItem: FeedItem) = {
    assert(feedItem.getId.id.isTagCollection)
    component.withProps(Props(feedItem))
  }

}
