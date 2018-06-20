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
import resonator.server.services.DotableService
import resonator.shared.constants.DotableKinds
import resonator.shared.mapper.PaginationInfoMapper
import resonator.shared.mapper.TagMapper
import resonator.shared.model.TagList
import resonator.shared.constants.DotableKinds.DotableKind
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class TagListFeedFetcher @Inject()(dotableService: DotableService)(implicit ec: ExecutionContext)
    extends FeedFetcher
    with LogSupport {
  override def fetch(params: FeedParams): Future[Feed] = {
    assert(params.feedId.id.isTagList)

    for {
      feedItem <- fetchListItem(params)
    } yield Feed(id = Some(params.feedId), items = Seq(feedItem))
  }

  private def fetchListItem(params: FeedParams): Future[FeedItem] = {
    val tagListId = params.feedId.getTagList
    val tagId = TagMapper.fromProto(tagListId.getTag).id
    val listLimit = params.maxItemSize
    val personId = params.loggedInUser.map(_.id)

    val paginationInfo = PaginationInfoMapper.fromProto(tagListId.getPaginationInfo)
    tagListId.dotableKind match {
      case Dotable.Kind.PODCAST_EPISODE => {
        dotableService
          .readEpisodeTagList(tagId, paginationInfo)
          .map(toListFeedItem(DotableKinds.PodcastEpisode, tagListId))
      }
      case _ => {
        dotableService
          .readPodcastTagList(DotableKinds.Podcast, tagId, paginationInfo, params.loggedInUser)
          .map(toListFeedItem(DotableKinds.Podcast, tagListId))
      }
    }
  }

  private def toListFeedItem(kind: DotableKind, tagListId: TagListId)(tagList: TagList): FeedItem = {
    if (kind == DotableKinds.PodcastEpisode && tagList.tag.id == MetadataFlag.Ids.popular) {
      toTagListFeedItem("New Episodes", "From Popular Podcasts", tagListId, tagList.list)
    } else {
      toTagListFeedItem(tagList.tag.name, "Ordered by Newest Episode", tagListId, tagList.list)
    }
  }

  private def toTagListFeedItem(title: String,
                                caption: String,
                                tagListId: TagListId,
                                list: Seq[Dotable]): FeedItem = {
    val feedList = FeedDotableList(
      Some(DotableList(title = title, caption = caption, dotables = list)),
      style = FeedDotableList.Style.PRIMARY)
    FeedItem()
      .withId(FeedId().withTagList(tagListId))
      .withContent(FeedItem.Content.DotableList(feedList))
  }
}
