package kurtome.dote.server.controllers.feed
import kurtome.dote.proto.api.feed.{Feed, FeedId}

import scala.concurrent.{ExecutionContext, Future}
import javax.inject._

import kurtome.dote.proto.api.feed.FeedId.HomeId
import kurtome.dote.server.model.{MetadataFlag, TagId}
import kurtome.dote.server.services.DotableService
import kurtome.dote.slick.db.{DotableKinds, TagKinds}

@Singleton
class HomeFeedFetcher @Inject()(dotableService: DotableService)(implicit ec: ExecutionContext)
    extends FeedFetcher {
  override def fetch(params: FeedParams): Future[Feed] = {
    val listLimit = params.maxItemSize
    val personId = params.loggedInUser.map(_.id)

    val newEpisodes = dotableService
      .readRecentEpisodes(MetadataFlag.Ids.popular, listLimit) map { episodes =>
      toListFeedItem("New Episodes", episodes)
    }

    val popularList = dotableService
      .readTagList(DotableKinds.Podcast, MetadataFlag.Ids.popular, listLimit, personId)
      .map(toListFeedItem)

    val nprList = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastCreator, "npr"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val comedy = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "comedy"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val crookedMediaList = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastCreator, "crooked-media"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val arts = dotableService
      .readTagList(DotableKinds.Podcast, TagId(TagKinds.PodcastGenre, "arts"), listLimit, personId)
      .map(toListFeedItem)

    val technology = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "technology"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val music = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "music"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val gimlet = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastCreator, "gimlet"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val newsAndPolitics = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "news-politics"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val tvAndFilm = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "tv-film"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val societyAndCulture = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "society-culture"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val sportsAndRecreation = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "sports-recreation"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val wnyc = dotableService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastCreator, "wnyc-studios"),
                   listLimit,
                   personId)
      .map(toListFeedItem)

    val theRinger = dotableService
      .readTagList(DotableKinds.Podcast,
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
}
