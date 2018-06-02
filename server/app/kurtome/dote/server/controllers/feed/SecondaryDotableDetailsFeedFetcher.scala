package kurtome.dote.server.controllers.feed

import javax.inject._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable_list.DotableList
import kurtome.dote.proto.api.feed.Feed
import kurtome.dote.proto.api.feed.FeedDotableList
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.TagListId
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.server.model.MetadataFlag
import kurtome.dote.server.search.SearchClient
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.util.UrlIds
import kurtome.dote.server.util.UrlIds.IdKinds
import kurtome.dote.shared.constants.DotableKinds
import kurtome.dote.shared.constants.DotableKinds.DotableKind
import kurtome.dote.shared.mapper.PaginationInfoMapper
import kurtome.dote.shared.mapper.TagMapper
import kurtome.dote.shared.model.TagList
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
