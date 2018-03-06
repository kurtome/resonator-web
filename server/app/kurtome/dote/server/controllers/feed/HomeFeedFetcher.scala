package kurtome.dote.server.controllers.feed
import kurtome.dote.proto.api.feed.Feed

import scala.concurrent.{ExecutionContext, Future}
import javax.inject._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable_list.DotableList
import kurtome.dote.proto.api.feed.FeedDotableList
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.proto.api.feed.FeedId.TagListId
import kurtome.dote.server.db.mappers.DotableMapper
import kurtome.dote.shared.constants.TagKinds
import kurtome.dote.server.model.MetadataFlag
import kurtome.dote.server.services.DotableService
import kurtome.dote.shared.mapper.TagMapper
import kurtome.dote.shared.model.Tag
import kurtome.dote.shared.model.TagId
import kurtome.dote.shared.model.TagList
import kurtome.dote.slick.db.DotableKinds.DotableKind
import kurtome.dote.slick.db.DotableKinds

@Singleton
class HomeFeedFetcher @Inject()(dotableService: DotableService)(implicit ec: ExecutionContext)
    extends FeedFetcher {
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

    val nprList = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastCreator, "npr"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val comedy = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastGenre, "comedy"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val crookedMediaList = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastCreator, "crooked-media"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val arts = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastGenre, "arts"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val technology = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastGenre, "technology"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val music = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastGenre, "music"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val gimlet = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastCreator, "gimlet"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val newsAndPolitics = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastGenre, "news-politics"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val tvAndFilm = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastGenre, "tv-film"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val societyAndCulture = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastGenre, "society-culture"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val sportsAndRecreation = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastGenre, "sports-recreation"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val wnyc = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastCreator, "wnyc-studios"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val theRinger = dotableService
      .readPodcastTagList(DotableKinds.Podcast,
                          TagId(TagKinds.PodcastCreator, "the-ringer"),
                          listLimit,
                          personId)
      .map(toListFeedItem)

    val lists = Future.sequence(
      Seq(
        newEpisodes,
        popularList,
        societyAndCulture,
        nprList,
        comedy,
        crookedMediaList,
        technology,
        gimlet,
        sportsAndRecreation,
        arts,
        music,
        wnyc,
        theRinger,
        newsAndPolitics,
        tvAndFilm
      ))

    lists map { lists =>
      val feedItems = lists.filter(_.getDotableList.getList.dotables.nonEmpty)
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
                                subtitle: String,
                                tag: Tag,
                                list: Seq[Dotable],
                                kind: DotableKind): FeedItem = {
    val feedList = FeedDotableList(
      Some(DotableList(title = title, subtitle = subtitle, dotables = list)))
    FeedItem()
      .withId(FeedId().withTagList(
        TagListId(tag = Some(TagMapper.toProto(tag)), dotableKind = DotableMapper.mapKind(kind))))
      .withContent(FeedItem.Content.DotableList(feedList))
  }
}
