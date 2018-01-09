package kurtome.dote.server.controllers.api

import javax.inject._

import dote.proto.api.action.get_feed_controller._
import dote.proto.api.dotable.Dotable
import dote.proto.api.dotable_list.DotableList
import dote.proto.api.feed._
import kurtome.dote.server.db.{DotableDbService, MetadataFlag, TagId, TagList}
import kurtome.dote.slick.db.{DotableKinds, TagKinds}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetFeedController @Inject()(cc: ControllerComponents, podcastDbService: DotableDbService)(
    implicit ec: ExecutionContext)
    extends ProtobufController[GetFeedRequest, GetFeedResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    GetFeedRequest.parseFrom(bytes)

  override def action(request: Request[GetFeedRequest]) = {
    val popularList = podcastDbService
      .readTagList(DotableKinds.Podcast, MetadataFlag.Ids.popular, request.body.maxItemSize)

    val nprList = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastCreator, "npr"),
                   request.body.maxItemSize)

    val comedy = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "comedy"),
                   request.body.maxItemSize)

    val crookedMediaList = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastCreator, "crooked-media"),
                   request.body.maxItemSize)

    val arts = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "arts"),
                   request.body.maxItemSize)

    val technology = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "technology"),
                   request.body.maxItemSize)

    val music = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "music"),
                   request.body.maxItemSize)

    val gimlet = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastCreator, "gimlet"),
                   request.body.maxItemSize)

    val newsAndPolitics = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "news-politics"),
                   request.body.maxItemSize)

    val tvAndFilm = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "tv-film"),
                   request.body.maxItemSize)

    val societyAndCulture = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "society-culture"),
                   request.body.maxItemSize)

    val sportsAndRecreation = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastGenre, "sports-recreation"),
                   request.body.maxItemSize)

    val wnyc = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastCreator, "wnyc-studios"),
                   request.body.maxItemSize)

    val theRinger = podcastDbService
      .readTagList(DotableKinds.Podcast,
                   TagId(TagKinds.PodcastCreator, "the-ringer"),
                   request.body.maxItemSize)

    val lists = Future.sequence(
      Seq(
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
      val feedItems = lists.filter(_.isDefined).map(_.get) map toListFeedItem
      GetFeedResponse(feed = Some(Feed(feedItems)))
    }
  }

  private def toListFeedItem(list: TagList): FeedItem = {
    val feedList = FeedDotableList(Some(DotableList(title = list.tag.name, dotables = list.list)))
    FeedItem(kind = FeedItem.Kind.DOTABLE_LIST, content = FeedItem.Content.DotableList(feedList))
  }

}
