package resonator.server.controllers.feed

import javax.inject._
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.dotable_list.DotableList
import resonator.proto.api.feed.Feed
import resonator.proto.api.feed.FeedDotableList
import resonator.proto.api.feed.FeedId
import resonator.proto.api.feed.FeedId.TagListId
import resonator.proto.api.feed.FeedItem
import resonator.server.model.MetadataFlag
import resonator.server.search.SearchClient
import resonator.server.services.DotableService
import resonator.server.util.UrlIds
import resonator.server.util.UrlIds.IdKinds
import resonator.shared.constants.DotableKinds
import resonator.shared.constants.DotableKinds.DotableKind
import resonator.shared.mapper.PaginationInfoMapper
import resonator.shared.mapper.TagMapper
import resonator.shared.model.TagList
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class SecondaryDotableDetailsFeedFetcher @Inject()(
    dotableService: DotableService,
    searchClient: SearchClient)(implicit ec: ExecutionContext)
    extends FeedFetcher
    with LogSupport {

  override def fetch(params: FeedParams): Future[Feed] = {
    assert(params.feedId.id.isSecondaryDotableDetails)

    val dotableId =
      UrlIds.decode(IdKinds.Dotable, params.feedId.getSecondaryDotableDetails.dotableId)
    for {
      dotableOpt <- dotableService.readDotableDetails(dotableId, None)
      moreLike <- dotableOpt match {
        case Some(dotable) => searchClient.moreLike(dotable)
        case _ => Future(Nil)
      }
    } yield
      dotableOpt match {
        case Some(dotable) => {
          val moreLikeTitle =
            s"Similar ${if (dotable.kind == Dotable.Kind.PODCAST) "Podcasts" else "Epsidoes"}"
          Feed(
            items = moreLike match {
              case Nil => Nil
              case _ =>
                Seq(
                  FeedItem()
                    .withDotableList(FeedDotableList().withList(
                      DotableList(dotables = moreLike, title = moreLikeTitle)))
                    .withId(FeedId().withDotableList(FeedId.DotableListId())))
            }
          ).withId(params.feedId)
        }
        case _ => Feed().withId(params.feedId)
      }
  }

}
