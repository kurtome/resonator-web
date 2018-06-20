package resonator.server.controllers.feed
import resonator.proto.api.feed.Feed

import scala.concurrent.{ExecutionContext, Future}
import javax.inject._
import resonator.proto.api.activity.Activity
import resonator.proto.api.activity.ActivityList
import resonator.proto.api.activity.DoteActivity
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.dotable_list.DotableList
import resonator.proto.api.dote.Dote
import resonator.proto.api.feed.FeedActivityList
import resonator.proto.api.feed.FeedDotableList
import resonator.proto.api.feed.FeedId
import resonator.proto.api.feed.FeedId.ActivityId
import resonator.proto.api.feed.FeedId.TagCollectionId
import resonator.proto.api.feed.FeedItem
import resonator.proto.api.feed.FeedId.TagListId
import resonator.proto.api.feed.FeedItemCommon
import resonator.proto.api.feed.FeedTagCollection
import resonator.proto.api.tag
import resonator.proto.api.tag.TagCollection
import resonator.server.db.mappers.DotableMapper
import resonator.server.db.mappers.DoteMapper
import resonator.shared.constants.TagKinds
import resonator.server.model.MetadataFlag
import resonator.server.search.SearchClient
import resonator.server.services.DotableService
import resonator.server.services.DoteService
import resonator.server.services.TagService
import resonator.shared.constants.DotableKinds
import resonator.shared.mapper.TagMapper
import resonator.shared.model.PaginationInfo
import resonator.shared.model.Tag
import resonator.shared.model.TagId
import resonator.shared.model.TagList
import resonator.shared.constants.DotableKinds.DotableKind
import resonator.slick.db.gen.Tables
import wvlet.log.LogSupport

@Singleton
class HomeFeedFetcher @Inject()(doteService: DoteService,
                                dotableService: DotableService,
                                tagService: TagService,
                                searchClient: SearchClient)(implicit ec: ExecutionContext)
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

    val paginationInfo = PaginationInfo(listLimit)

    val recentActivity = doteService
      .readRecentDotesWithDotables(paginationInfo)
      .map(_.map(ActivityFeedFetcher.mapActivityData)) map { list =>
      ActivityFeedFetcher.toActivityListFeedItem(FeedId().withActivity(FeedId.ActivityId()),
                                                 "Recent Activity",
                                                 "",
                                                 list,
                                                 backgroundColor =
                                                   FeedItemCommon.BackgroundColor.DEFAULT)
    }

    val recentActivityFromFollowing = personId
      .map(doteService.recentDotesWithDotableFromFollowing(paginationInfo, _))
      .getOrElse(Future(Nil))
      .map(_.map(ActivityFeedFetcher.mapActivityData)) map { list =>
      ActivityFeedFetcher.toActivityListFeedItem(
        FeedId().withActivity(FeedId.ActivityId().withFollowingOnly(true)),
        "Recent From Following",
        "",
        list,
        backgroundColor = FeedItemCommon.BackgroundColor.DEFAULT
      )
    }

    val newEpisodes = dotableService
      .readEpisodeTagList(MetadataFlag.Ids.popular, paginationInfo) map { tagList =>
      toTagListFeedItem("New Episodes",
                        "From Popular Podcasts",
                        tagList.tag,
                        tagList.list,
                        DotableKinds.PodcastEpisode,
                        FeedItemCommon.BackgroundColor.DEFAULT)
    }

    val popularList = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          MetadataFlag.Ids.popular,
                          paginationInfo,
                          params.loggedInUser)
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
        recentActivity,
        recentActivityFromFollowing,
        popularList,
        creatorsTagCollection,
        categoriesTagCollection
      ))

    lists map { lists =>
      val feedItems = lists.filter(validFeedItem)
      Feed(id = Some(params.feedId), items = feedItems)
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
                                kind: DotableKind,
                                backgroundColor: FeedItemCommon.BackgroundColor =
                                  FeedItemCommon.BackgroundColor.DEFAULT): FeedItem = {
    val feedList =
      FeedDotableList().withList(DotableList(title = title, caption = caption, dotables = list))
    FeedItem()
      .withId(FeedId().withTagList(
        TagListId(tag = Some(TagMapper.toProto(tag)), dotableKind = DotableMapper.mapKind(kind))))
      .withContent(FeedItem.Content.DotableList(feedList))
      .withCommon(FeedItemCommon.defaultInstance.withBackgroundColor(backgroundColor))
  }

  private def toTagCollectionFeedItem(title: String, list: Seq[Tables.TagRow]): FeedItem = {
    val collection = FeedTagCollection(
      Some(TagCollection(title = title, tagsFetched = true, tags = list map { row =>
        TagMapper.toProto(Tag(row.kind, row.key, row.name))
      })))
    FeedItem()
      .withCommon(FeedItemCommon(backgroundColor = FeedItemCommon.BackgroundColor.PRIMARY))
      .withId(FeedId().withTagCollection(TagCollectionId()))
      .withContent(FeedItem.Content.TagCollection(collection))
  }
}
