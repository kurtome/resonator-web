package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.proto.api.action.get_feed._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable_list.DotableList
import kurtome.dote.proto.api.feed._
import kurtome.dote.server.model.{MetadataFlag, TagId, TagList, _}
import kurtome.dote.server.services.{AuthTokenService, DotableService}
import kurtome.dote.slick.db.{DotableKinds, TagKinds}
import play.api.mvc._
import wvlet.log.LogSupport

import scala.concurrent._

@Singleton
class GetFeedController @Inject()(
    cc: ControllerComponents,
    podcastDbService: DotableService,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetFeedRequest, GetFeedResponse](cc)
    with LogSupport {

  override def parseRequest(bytes: Array[Byte]) =
    GetFeedRequest.parseFrom(bytes)

  override def action(request: Request[GetFeedRequest]) =
    authTokenService.simplifiedRead(request) flatMap { loggedInPerson =>
      // TODO: refactor this so it's easier to use the logged in person's ID
      val personId = loggedInPerson.map(_.id)

      val listLimit = request.body.maxItemSize

      val newEpisodes = podcastDbService
        .readRecentEpisodes(MetadataFlag.Ids.popular, listLimit) map { episodes =>
        toListFeedItem("New Episodes", episodes)
      }

      val popularList = podcastDbService
        .readTagList(DotableKinds.Podcast, MetadataFlag.Ids.popular, listLimit, personId)
        .map(toListFeedItem)

      val nprList = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastCreator, "npr"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val comedy = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastGenre, "comedy"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val crookedMediaList = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastCreator, "crooked-media"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val arts = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastGenre, "arts"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val technology = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastGenre, "technology"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val music = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastGenre, "music"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val gimlet = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastCreator, "gimlet"),
                     listLimit,
                     loggedInPerson.map(_.id))
        .map(toListFeedItem)

      val newsAndPolitics = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastGenre, "news-politics"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val tvAndFilm = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastGenre, "tv-film"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val societyAndCulture = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastGenre, "society-culture"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val sportsAndRecreation = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastGenre, "sports-recreation"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val wnyc = podcastDbService
        .readTagList(DotableKinds.Podcast,
                     TagId(TagKinds.PodcastCreator, "wnyc-studios"),
                     listLimit,
                     personId)
        .map(toListFeedItem)

      val theRinger = podcastDbService
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
        GetFeedResponse(feed = Some(Feed(feedItems)))
      }
    }

  private def toListFeedItem(list: TagList): FeedItem = {
    val feedList = FeedDotableList(Some(DotableList(title = list.tag.name, dotables = list.list)))
    FeedItem(kind = FeedItem.Kind.DOTABLE_LIST, content = FeedItem.Content.DotableList(feedList))
  }

  private def toListFeedItem(title: String, list: Seq[Dotable]): FeedItem = {
    val feedList = FeedDotableList(Some(DotableList(title = title, dotables = list)))
    FeedItem(kind = FeedItem.Kind.DOTABLE_LIST, content = FeedItem.Content.DotableList(feedList))
  }

}
