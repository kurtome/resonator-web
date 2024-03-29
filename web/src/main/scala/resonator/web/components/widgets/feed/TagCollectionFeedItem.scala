package resonator.web.components.widgets.feed

import japgolly.scalajs.react.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.feed.FeedId.TagListId
import resonator.proto.api.feed.FeedItem
import resonator.web.CssSettings._
import resonator.web.DoteRoutes._
import resonator.web.components.ComponentHelpers._
import resonator.web.components.materialui._
import resonator.web.components.widgets.FlatRoundedButton
import resonator.web.components.widgets.MainContentSection
import resonator.web.utils.BaseBackend
import resonator.web.utils.FeedIdRoutes.TagRouteMapper
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

      MainContentSection(variant = MainContentSection.chooseVariant(p.feedItem.getCommon))(
        GridContainer(spacing = 8)(
          GridItem(xs = 12)(
            Typography(variant = Typography.Variants.Title)(collection.title)
          ),
          (tags map { tag =>
            GridItem(key = Some(tag.getId.kind.toString + tag.getId.key))(
              FlatRoundedButton(
                variant = FlatRoundedButton.Variants.FillLight,
                onClick = doteRouterCtl.set(TagRouteMapper.toRoute(TagListId().withTag(tag))))(
                tag.displayValue)
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
