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
import scala.util.Random

@Singleton
class DotableDbService @Inject()(db: BasicBackend#Database,
                                 dotableDbIo: DotableDbIo,
                                 podcastFeedIngestionDbIo: PodcastFeedIngestionDbIo,
                                 tagDbIo: DotableTagDbIo)(implicit ec: ExecutionContext) {

  lazy val popularTagId: Future[Long] = db.run(tagDbIo.readTagIdByKeyRaw("popular")).map(_.get)

  def setPopularTag(dotableId: Long) = {
    popularTagId map { tagId =>
      db.run(tagDbIo.dotableTagExists(tagId, dotableId)) map { exists =>
        if (!exists) {
          db.run(tagDbIo.insertDotableTag(tagId, dotableId))
        }
      }
    }
  }

  def ingestPodcast(itunesId: Long, podcast: RssFetchedPodcast): Future[Long] = {
    // Ignore episodes without a GUID
    val episodesWithGuids = podcast.episodes.filter(_.details.rssGuid.size > 0)
    // Dedupe GUIDs, just throw away duplicate episodes
    val episodes = episodesWithGuids
      .groupBy(_.details.rssGuid)
      .map(pair => {
        val guid = pair._1
        val dupes = pair._2
        dupes.sortBy(_.common.publishedEpochSec).head
      })
      .toSeq

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

  def readLimited(kind: DotableKinds.Value, limit: Int): Future[Seq[Dotable]] = {
    db.run(dotableDbIo.readLimited(kind, limit))
  }

  def readByTagKey(kind: DotableKinds.Value, tagKey: String, limit: Long): Future[Seq[Dotable]] = {
    val query = for {
      ids <- tagDbIo.readDotableIdsByTagKey(tagKey, limit)
      dotables <- dotableDbIo.readByIdBatch(kind, Set(ids: _*))
    } yield dotables
    db.run(query)
  }

  def readLimitedRandom(kind: DotableKinds.Value, limit: Int): Future[Seq[Dotable]] = {
    db.run(dotableDbIo.maxId()) flatMap { maxId =>
      if (maxId.isDefined) {
        val maxValidId = Math.max(0, Math.round(Random.nextDouble() / 2 * maxId.get) - limit)
        db.run(dotableDbIo.readLimited(kind, limit, maxValidId))
      } else {
        Future(Nil)
      }
    }
  }

  def readPodcastWithEpisodes(id: Long): Future[Option[Dotable]] = {
    val op = for {
      podcast <- dotableDbIo.readHeadById(DotableKinds.Podcast, id)
      episodes <- dotableDbIo.readByParentId(DotableKinds.PodcastEpisode, id)
    } yield podcast.map(_.update(_.relatives.children := episodes))
    db.run(op)
  }

  def readDotableWithParentAndChildren(id: Long): Future[Option[Dotable]] = {
    val op = for {
      dotableOpt <- dotableDbIo.readHeadById(id)
      children <- dotableDbIo.readByParentId(id)
      parentOpt <- dotableDbIo.readByChildId(DotableKinds.Podcast, id)
    } yield
      dotableOpt.map(
        _.update(_.relatives.children := children)
          .update(_.relatives.parent := parentOpt.getOrElse(Dotable.defaultInstance)))
    db.run(op)
  }

  private def matchToExistingEpisodeId(episodes: Seq[RssFetchedEpisode],
                                       existingEpisodes: Seq[Tables.PodcastEpisodeIngestionRow])
    : Seq[(Option[Long], RssFetchedEpisode)] = {
    val guidMap = existingEpisodes.map(row => row.guid -> row.episodeDotableId).toMap
    episodes.map(episode => guidMap.get(episode.details.rssGuid) -> episode)
  }
}
