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
import kurtome.dote.server.services.DotableService
import kurtome.dote.shared.mapper.TagMapper
import kurtome.dote.shared.model.Tag
import kurtome.dote.shared.model.TagList
import kurtome.dote.slick.db.DotableKinds.DotableKind
import kurtome.dote.slick.db.DotableKinds

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class TagListFeedFetcher @Inject()(dotableService: DotableService)(implicit ec: ExecutionContext)
    extends FeedFetcher {
  override def fetch(params: FeedParams): Future[Feed] = {
    assert(params.feedId.id.isTagList)

    val tagListId = params.feedId.getTagList
    val tagId = TagMapper.fromProto(tagListId.getTag).id
    val listLimit = params.maxItemSize
    val personId = params.loggedInUser.map(_.id)

    for {
      feedItem <- dotableService
        .readTagList(DotableKinds.Podcast, tagId, listLimit, personId)
        .map(toListFeedItem)
    } yield Feed(id = Some(params.feedId), items = Seq(feedItem))
  }

  private def toListFeedItem(tagList: TagList): FeedItem = {
    toTagListFeedItem(tagList.tag.name, tagList.tag, tagList.list)
  }

  private def toTagListFeedItem(title: String,
                                tag: Tag,
                                list: Seq[Dotable],
                                kind: DotableKind = DotableKinds.Podcast): FeedItem = {
    val feedList = FeedDotableList(Some(DotableList(title = title, dotables = list)),
                                   style = FeedDotableList.Style.PRIMARY)
    FeedItem()
      .withId(FeedId().withTagList(
        TagListId(tag = Some(TagMapper.toProto(tag)), dotableKind = DotableMapper.mapKind(kind))))
      .withContent(FeedItem.Content.DotableList(feedList))
  }
}
