package kurtome.dote.server.controllers.feed

import javax.inject._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable_list.DotableList
import kurtome.dote.proto.api.feed.Feed
import kurtome.dote.proto.api.feed.FeedDotableList
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.proto.api.feed.FeedItemId
import kurtome.dote.proto.api.feed.FeedItemId.FollowerSummaryId
import kurtome.dote.proto.api.feed.FeedItemId.ProfileDoteListId
import kurtome.dote.proto.api.follower.FollowerSummary
import kurtome.dote.server.controllers.follow.FollowApiHelper
import kurtome.dote.server.db.mappers.DotableMapper
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.services.PersonService
import kurtome.dote.shared.constants.Emojis
import kurtome.dote.slick.db.gen.Tables.PersonRow
import kurtome.dote.slick.db.DotableKinds
import kurtome.dote.slick.db.DotableKinds.DotableKind
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfileFeedFetcher @Inject()(dotableService: DotableService,
                                   personService: PersonService,
                                   followApiHelper: FollowApiHelper)(implicit ec: ExecutionContext)
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

    val followerSummary = followApiHelper.getSummary(personRow).map(toFollowerSummaryFeedItem)

    val smilePodcasts = dotableService
      .readPersonSmileList(personRow.id, DotableKinds.Podcast, feedParams.maxItemSize) map {
      list =>
        toProfileListFeedItem(username,
                              s"$username's $smileEmojis Podcasts",
                              ProfileDoteListId.Kind.SMILE,
                              list,
                              DotableKinds.Podcast)
    }

    val smileEpisodes = dotableService
      .readPersonSmileList(personRow.id, DotableKinds.PodcastEpisode, feedParams.maxItemSize) map {
      list =>
        toProfileListFeedItem(username,
                              s"$username's $smileEmojis Episodes",
                              ProfileDoteListId.Kind.SMILE,
                              list,
                              DotableKinds.PodcastEpisode)
    }

    val laughPodcasts = dotableService
      .readPersonLaughList(personRow.id, DotableKinds.Podcast, feedParams.maxItemSize) map {
      list =>
        toProfileListFeedItem(username,
                              s"$username's $laughEmojis Podcasts",
                              ProfileDoteListId.Kind.LAUGH,
                              list,
                              DotableKinds.Podcast)
    }

    val laughEpisodes = dotableService
      .readPersonLaughList(personRow.id, DotableKinds.PodcastEpisode, feedParams.maxItemSize) map {
      list =>
        toProfileListFeedItem(username,
                              s"$username's $laughEmojis Episodes",
                              ProfileDoteListId.Kind.LAUGH,
                              list,
                              DotableKinds.PodcastEpisode)
    }

    val cryPodcasts = dotableService
      .readPersonCryList(personRow.id, DotableKinds.Podcast, feedParams.maxItemSize) map { list =>
      toProfileListFeedItem(username,
                            s"$username's $cryEmojis Podcasts",
                            ProfileDoteListId.Kind.CRY,
                            list,
                            DotableKinds.Podcast)
    }

    val cryEpisodes = dotableService
      .readPersonCryList(personRow.id, DotableKinds.PodcastEpisode, feedParams.maxItemSize) map {
      list =>
        toProfileListFeedItem(username,
                              s"$username's $cryEmojis Episodes",
                              ProfileDoteListId.Kind.CRY,
                              list,
                              DotableKinds.PodcastEpisode)
    }

    val scowlPodcasts = dotableService
      .readPersonScowlList(personRow.id, DotableKinds.Podcast, feedParams.maxItemSize) map {
      list =>
        toProfileListFeedItem(username,
                              s"$username's $scowlEmojis Podcasts",
                              ProfileDoteListId.Kind.SCOWL,
                              list,
                              DotableKinds.Podcast)
    }

    val scowlEpisodes = dotableService
      .readPersonScowlList(personRow.id, DotableKinds.PodcastEpisode, feedParams.maxItemSize) map {
      list =>
        toProfileListFeedItem(username,
                              s"$username's $scowlEmojis Episodes",
                              ProfileDoteListId.Kind.SCOWL,
                              list,
                              DotableKinds.PodcastEpisode)
    }

    val feedItemsFuture = Future.sequence(
      Seq(
        followerSummary,
        smilePodcasts,
        smileEpisodes,
        cryPodcasts,
        cryEpisodes,
        laughPodcasts,
        laughEpisodes,
        scowlPodcasts,
        scowlEpisodes
      ))

    for {
      lists <- feedItemsFuture
      feedItems = lists.filter(item =>
        item.getId.id match {
          case FeedItemId.Id.TagListId(_) => {
            // only include non-empty lists
            item.getDotableList.getList.dotables.nonEmpty
          }
          case _ => true
      })
    } yield Feed(id = Some(feedParams.feedId), items = feedItems)
  }

  private def toFollowerSummaryFeedItem(followerSummary: FollowerSummary) = {
    FeedItem()
      .withId(
        FeedItemId().withFollowerSummaryId(
          FollowerSummaryId(username = followerSummary.getPerson.username)))
      .withContent(FeedItem.Content.FollowerSummary(followerSummary))
  }

  private def toProfileListFeedItem(username: String,
                                    title: String,
                                    listKind: ProfileDoteListId.Kind,
                                    list: Seq[Dotable],
                                    kind: DotableKind = DotableKinds.Podcast): FeedItem = {
    val feedList = FeedDotableList(Some(DotableList(title = title, dotables = list)))
    FeedItem()
      .withId(
        FeedItemId().withProfileDoteListId(
          ProfileDoteListId(username = username,
                            listKind = listKind,
                            dotableKind = DotableMapper.mapKind(kind))))
      .withContent(FeedItem.Content.DotableList(feedList))
  }
}
