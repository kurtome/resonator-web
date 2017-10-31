package kurtome.dote.server.db

import javax.inject._

import dote.proto.api.dotable.Dotable
import kurtome.dote.server.controllers.podcast.{RssFetchedEpisode, RssFetchedPodcast}
import kurtome.dote.slick.db.DotableKinds
import slick.basic.BasicBackend
import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.gen.Tables
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PodcastDbService @Inject()(
    db: BasicBackend#Database,
    dotableDbIo: DotableDbIo,
    podcastFeedIngestionDbIo: PodcastFeedIngestionDbIo)(implicit ec: ExecutionContext) {

  def ingestPodcast(itunesId: Long, podcast: RssFetchedPodcast): Future[Long] = {
    // Ignore episodes without a guid
    val episodes = podcast.episodes.filter(_.details.rssGuid.size > 0)

    db.run(podcastFeedIngestionDbIo.readPodcastIdFromItunesId(itunesId)) flatMap {
      existingPodcastId =>
        if (existingPodcastId.isDefined) {
          val podcastId = existingPodcastId.get
          ingestExistingPodcast(itunesId, podcast, episodes, podcastId)
        } else {
          ingestNewPodcast(itunesId, podcast, episodes)
        }
    }
  }

  private def ingestNewPodcast(itunesId: Long,
                               podcast: RssFetchedPodcast,
                               episodes: Seq[RssFetchedEpisode]) = {
    // New podcast, insert
    val insertPodcastRowOp = (for {
      podcastId <- dotableDbIo.insertAndGetId(podcast)
      _ <- podcastFeedIngestionDbIo.insert(podcastId, itunesId, podcast.feedUrl)
      episodeIdAndGuids <- dotableDbIo.insertEpisodeBatch(podcastId, episodes)
      _ <- podcastFeedIngestionDbIo.insertEpisodeRecords(podcastId, episodeIdAndGuids)
    } yield podcastId).transactionally
    db.run(insertPodcastRowOp)
  }

  private def ingestExistingPodcast(itunesId: Long,
                                    podcast: RssFetchedPodcast,
                                    episodes: Seq[RssFetchedEpisode],
                                    podcastId: Long) = {
    // Podcast already exists, update it
    val insertPodcastRowOp = (for {
      existingEpisodes <- podcastFeedIngestionDbIo.readEpisodesByPodcastId(podcastId)
      episodesWithExistingId = matchToExistingEpisodeId(episodes, existingEpisodes)
      newEpisodes = episodesWithExistingId.filter(!_._1.isDefined).map(_._2)
      existingEpisodesWithId = episodesWithExistingId
        .filter(_._1.isDefined)
        .map(pair => pair._1.get -> pair._2)
      _ <- dotableDbIo.updateExisting(podcastId, podcast)
      _ <- podcastFeedIngestionDbIo.updateFeedUrByItunesId(itunesId, podcast.feedUrl)
      newEpisodesAndGuids <- dotableDbIo.insertEpisodeBatch(podcastId, newEpisodes)
      _ <- podcastFeedIngestionDbIo.insertEpisodeRecords(podcastId, newEpisodesAndGuids)
      _ <- dotableDbIo.updateEpisodes(podcastId, existingEpisodesWithId)
    } yield ()).transactionally
    db.run(insertPodcastRowOp).map(_ => podcastId)
  }

  def readPodcastWithEpisodes(id: Long): Future[Option[Dotable]] = {
    val op = for {
      podcast <- dotableDbIo.readHeadById(DotableKinds.Podcast, id)
      episodes <- dotableDbIo.readByParentId(DotableKinds.PodcastEpisode, id)
    } yield podcast.map(_.update(_.relatives.children := episodes))
    db.run(op)
  }

  private def matchToExistingEpisodeId(episodes: Seq[RssFetchedEpisode],
                                       existingEpisodes: Seq[Tables.PodcastEpisodeIngestionRow])
    : Seq[(Option[Long], RssFetchedEpisode)] = {
    val guidMap = existingEpisodes.map(row => row.guid -> row.episodeDotableId).toMap
    Logger.info(s"existing guids $guidMap")
    episodes.map(episode => guidMap.get(episode.details.rssGuid) -> episode)
  }
}
