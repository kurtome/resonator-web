package kurtome.dote.server.controllers.feed

import javax.inject._

import kurtome.dote.proto.api.feed.Feed
import kurtome.dote.proto.api.feed.FeedId.HomeId
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.services.PersonService
import kurtome.dote.shared.constants.Emojis
import kurtome.dote.slick.db.gen.Tables.PersonRow
import kurtome.dote.slick.db.DotableKinds
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfileFeedFetcher @Inject()(dotableService: DotableService, personService: PersonService)(
    implicit ec: ExecutionContext)
    extends FeedFetcher
    with LogSupport {

  override def fetch(params: FeedParams): Future[Feed] = {
    personService.readByUsername(params.feedId.getProfileId.username) flatMap {
      case Some(personRow) => fetchForPerson(personRow, params)
      case None => Future(Feed.defaultInstance)
    }
  }

  def fetchForPerson(personRow: PersonRow, feedParams: FeedParams): Future[Feed] = {
    val username = personRow.username

    val smileEmojis = Emojis.smileEmojis.mkString(" ")
    val laughEmojis = Emojis.laughEmojis.mkString(" ")
    val cryEmojis = Emojis.cryEmojis.mkString(" ")
    val scowlEmojis = Emojis.scowlEmojis.mkString(" ")

    val smilePodcasts = dotableService
      .readPersonSmileList(personRow.id, DotableKinds.Podcast, feedParams.maxItemSize) map {
      list =>
        toListFeedItem(s"$username's $smileEmojis Podcasts", list)
    }

    val smileEpisodes = dotableService
      .readPersonSmileList(personRow.id, DotableKinds.PodcastEpisode, feedParams.maxItemSize) map {
      list =>
        toListFeedItem(s"$username's $smileEmojis Episodes", list)
    }

    val laughPodcasts = dotableService
      .readPersonLaughList(personRow.id, DotableKinds.Podcast, feedParams.maxItemSize) map {
      list =>
        toListFeedItem(s"$username's $laughEmojis Podcasts", list)
    }

    val laughEpisodes = dotableService
      .readPersonLaughList(personRow.id, DotableKinds.PodcastEpisode, feedParams.maxItemSize) map {
      list =>
        toListFeedItem(s"$username's $laughEmojis Episodes", list)
    }

    val cryPodcasts = dotableService
      .readPersonCryList(personRow.id, DotableKinds.Podcast, feedParams.maxItemSize) map { list =>
      toListFeedItem(s"$username's $cryEmojis Podcasts", list)
    }

    val cryEpisodes = dotableService
      .readPersonCryList(personRow.id, DotableKinds.PodcastEpisode, feedParams.maxItemSize) map {
      list =>
        toListFeedItem(s"$username's $cryEmojis Episodes", list)
    }

    val scowlPodcasts = dotableService
      .readPersonScowlList(personRow.id, DotableKinds.Podcast, feedParams.maxItemSize) map {
      list =>
        toListFeedItem(s"$username's $scowlEmojis Podcasts", list)
    }

    val scowlEpisodes = dotableService
      .readPersonScowlList(personRow.id, DotableKinds.PodcastEpisode, feedParams.maxItemSize) map {
      list =>
        toListFeedItem(s"$username's $scowlEmojis Episodes", list)
    }

    val lists = Future.sequence(
      Seq(
        smilePodcasts,
        smileEpisodes,
        cryPodcasts,
        cryEpisodes,
        laughPodcasts,
        laughEpisodes,
        scowlPodcasts,
        scowlEpisodes
      ))

    lists map { lists =>
      val feedItems = lists.filter(_.getDotableList.getList.dotables.nonEmpty)
      Feed(id = Some(feedParams.feedId), items = feedItems)
    }
  }
}
