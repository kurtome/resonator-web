package kurtome.dote.server.controllers.feed
import kurtome.dote.proto.api.feed.Feed

import scala.concurrent.{ExecutionContext, Future}
import javax.inject._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable_list.DotableList
import kurtome.dote.proto.api.feed.FeedDotableList
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedId.TagCollectionId
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.proto.api.feed.FeedId.TagListId
import kurtome.dote.proto.api.feed.FeedTagCollection
import kurtome.dote.proto.api.tag
import kurtome.dote.proto.api.tag.TagCollection
import kurtome.dote.server.db.mappers.DotableMapper
import kurtome.dote.shared.constants.TagKinds
import kurtome.dote.server.model.MetadataFlag
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.services.TagService
import kurtome.dote.shared.mapper.TagMapper
import kurtome.dote.shared.model.Tag
import kurtome.dote.shared.model.TagId
import kurtome.dote.shared.model.TagList
import kurtome.dote.slick.db.DotableKinds.DotableKind
import kurtome.dote.slick.db.DotableKinds
import kurtome.dote.slick.db.gen.Tables
import wvlet.log.LogSupport

@Singleton
class HomeFeedFetcher @Inject()(dotableService: DotableService, tagService: TagService)(
    implicit ec: ExecutionContext)
    extends FeedFetcher
    with LogSupport {

  private val topCategories = Seq(
    "religion-spirituality",
    "music",
    "comedy",
    "business",
    "society-culture",
    "sports-recreation",
    "tv-film",
    "news-politics",
    "health",
    "games-hobbies",
    "arts",
    "education",
    "technology",
    "kids-family"
  )

  private val topCreators = Seq(
    "gimlet",
    "the-ringer",
    "wnyc-studios",
    "the-new-york-times",
    "prx",
    "npr",
    "howstuffworks",
    "this-american-life",
    "joe-rogan",
    "espn",
    "wondery",
    "american-public-media",
    "earwolf",
    "slate",
    "relay-fm",
    "dan-carlan",
    "roman-mars"
  )

  override def fetch(params: FeedParams): Future[Feed] = {
    val listLimit = params.maxItemSize
    val personId = params.loggedInUser.map(_.id)

    val newEpisodes = dotableService
      .readEpisodeTagList(MetadataFlag.Ids.popular, listLimit) map { tagList =>
      toTagListFeedItem("New Episodes",
                        "From Popular Podcasts",
                        tagList.tag,
                        tagList.list,
                        DotableKinds.PodcastEpisode)
    }

    val popularList = dotableService
      .readPodcastTagList(DotableKinds.Podcast, MetadataFlag.Ids.popular, listLimit, personId)
      .map(toListFeedItem)

    val creatorsTagCollection = tagService
      .readTags(TagKinds.PodcastCreator, topCreators)
      .map(toTagCollectionFeedItem("Top Creators", _))

    val categoriesTagCollection = tagService
      .readTags(TagKinds.PodcastGenre, topCategories)
      .map(toTagCollectionFeedItem("Top Categories", _))

    val lists = Future.sequence(
      Seq(
        newEpisodes,
        popularList,
        creatorsTagCollection,
        categoriesTagCollection
      ))

    lists map { lists =>
      val feedItems = lists.filter(validFeedItem)
      Feed(id = Some(params.feedId), items = feedItems)
    }
  }

  private def validFeedItem(feedItem: FeedItem): Boolean = {
    feedItem.content match {
      case FeedItem.Content.DotableList(feedDotableList) => {
        feedDotableList.getList.dotables.nonEmpty
      }
      case FeedItem.Content.TagCollection(feedTagCollection) => {
        feedTagCollection.getTagCollection.tags.nonEmpty
      }
    }
  }

  private def toListFeedItem(tagList: TagList): FeedItem = {
    toTagListFeedItem(tagList.tag.name,
                      "Ordered by Newest Episode",
                      tagList.tag,
                      tagList.list,
                      DotableKinds.Podcast)
  }

  private def toTagListFeedItem(title: String,
                                caption: String,
                                tag: Tag,
                                list: Seq[Dotable],
                                kind: DotableKind): FeedItem = {
    val feedList = FeedDotableList(
      Some(DotableList(title = title, caption = caption, dotables = list)))
    FeedItem()
      .withId(FeedId().withTagList(
        TagListId(tag = Some(TagMapper.toProto(tag)), dotableKind = DotableMapper.mapKind(kind))))
      .withContent(FeedItem.Content.DotableList(feedList))
  }

  private def toTagCollectionFeedItem(title: String, list: Seq[Tables.TagRow]): FeedItem = {
    val collection = FeedTagCollection(
      Some(TagCollection(title = title, tagsFetched = true, tags = list map { row =>
        TagMapper.toProto(Tag(row.kind, row.key, row.name))
      })))
    FeedItem()
      .withId(FeedId().withTagCollection(TagCollectionId()))
      .withContent(FeedItem.Content.TagCollection(collection))
  }
}
