package resonator.web.components.widgets.feed

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import resonator.proto.api.action.get_feed._
import resonator.proto.api.feed.FeedId
import resonator.proto.api.feed._
import resonator.web.CssSettings._
import resonator.web.components.ComponentHelpers._
import resonator.web.rpc.ResonatorApiClient
import resonator.web.utils._
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object FeedContainer extends LogSupport {

  lazy val defaultPageSize: Int = currentBreakpointString match {
    case "xs" => 12
    case _ => 24
  }

  object Styles extends StyleSheet.Inline {
    import dsl._

  }

  case class Props(feedId: FeedId)
  case class State(feed: Feed = Feed.defaultInstance, isFeedLoading: Boolean = true)

  class Backend(bs: BackendScope[Props, State]) extends BaseBackend(Styles) {

    val handleDidUpdate = (p: Props) => Callback { fetchFeed(p) }

    def fetchFeed(p: Props) = {
      if (p.feedId != FeedId.defaultInstance) {
        bs.modState(_.copy(isFeedLoading = true, feed = Feed.defaultInstance)).runNow()

        // get the latest data as well, in case it has changed
        val f = ResonatorApiClient.getFeed(
          GetFeedRequest(maxItems = 10, maxItemSize = 12, id = Some(p.feedId))) map { response =>
          if (response.getFeed.getId == p.feedId) {
            bs.modState(_.copy(feed = response.getFeed, isFeedLoading = false)).runNow()
          }
        }
        GlobalLoadingManager.addLoadingFuture(f)
      } else {
        bs.modState(_.copy(isFeedLoading = false, feed = Feed.defaultInstance)).runNow()
      }
    }

    def render(p: Props, s: State): VdomElement = {
      VerticalFeed(s.feed, s.isFeedLoading)()
    }
  }

  val component = ScalaComponent
    .builder[Props](this.getClass.getSimpleName)
    .initialState(State())
    .backend(new Backend(_))
    .render(x => x.backend.render(x.props, x.state))
    .componentDidMount(x => x.backend.handleDidUpdate(x.props))
    .componentWillReceiveProps(x => x.backend.handleDidUpdate(x.nextProps))
    .build

  def apply(id: FeedId) = {
    component.withProps(Props(id))
  }
}
