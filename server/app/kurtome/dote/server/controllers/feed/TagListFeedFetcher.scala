package kurtome.dote.server.controllers.feed

import javax.inject._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable_list.DotableList
import kurtome.dote.proto.api.feed.Feed
import kurtome.dote.proto.api.feed.FeedDotableList
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.TagListId
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.server.db.mappers.DotableMapper
import kurtome.dote.server.model.MetadataFlag
import kurtome.dote.server.services.DotableService
import kurtome.dote.shared.mapper.TagMapper
import kurtome.dote.shared.model.Tag
import kurtome.dote.shared.model.TagList
import kurtome.dote.slick.db.DotableKinds.DotableKind
import kurtome.dote.slick.db.DotableKinds
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
    val offset = tagListId.pageIndex * listLimit

    debug(offset)

    tagListId.dotableKind match {
      case Dotable.Kind.PODCAST_EPISODE => {
        dotableService
          .readEpisodeTagList(tagId, offset, listLimit)
          .map(toListFeedItem(DotableKinds.PodcastEpisode, tagListId))
      }
      case _ => {
        dotableService
          .readPodcastTagList(DotableKinds.Podcast, tagId, offset, listLimit, personId)
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
