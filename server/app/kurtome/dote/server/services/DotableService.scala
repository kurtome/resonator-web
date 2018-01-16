package kurtome.dote.server.services

import java.time.LocalDateTime
import javax.inject._

import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.server.db._
import kurtome.dote.server.db.DotableDbIo
import kurtome.dote.server.db.mappers.{DotableMapper, DoteMapper}
import kurtome.dote.server.ingestion.{RssFetchedEpisode, RssFetchedPodcast}
import kurtome.dote.server.model
import kurtome.dote.slick.db.DotableKinds
import kurtome.dote.slick.db.DotableKinds.DotableKind
import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.gen.Tables
import slick.basic.BasicBackend
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class DotableService @Inject()(db: BasicBackend#Database,
                               dotableDbIo: DotableDbIo,
                               podcastFeedIngestionDbIo: PodcastFeedIngestionDbIo,
                               tagDbIo: DotableTagDbIo)(implicit ec: ExecutionContext)
    extends LogSupport {

  lazy val popularTagDbId: Future[Long] =
    db.run(tagDbIo.readTagDbId(model.MetadataFlag.Ids.popular)).map(_.get)

  def setPopularTag(dotableId: Long) = {
    popularTagDbId map { tagId =>
      db.run(tagDbIo.dotableTagExists(tagId, dotableId)) map { exists =>
        if (!exists) {
          db.run(tagDbIo.insertDotableTag(tagId, dotableId))
        }
      }
    }
  }

  def addFeedForLaterIngestion(itunesId: Long, feedUrl: String): Future[Unit] = {
    db.run(podcastFeedIngestionDbIo.insertIfNew(itunesId, feedUrl)).map(_ => Unit)
  }

  def ingestPodcast(itunesId: Long, podcast: RssFetchedPodcast): Future[Long] = {
    // Ignore episodes without a GUID
    val episodesWithGuids = podcast.episodes.filter(_.details.rssGuid.nonEmpty)
    // Dedupe GUIDs, just throw away duplicate episodes
    val episodes = episodesWithGuids
      .groupBy(_.details.rssGuid)
      .map(pair => {
        val guid = pair._1
        val dupes = pair._2
        dupes.minBy(_.common.publishedEpochSec)
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
      _ <- podcastFeedIngestionDbIo.insertIfNew(itunesId, podcast.feedUrl)
      row <- podcastFeedIngestionDbIo.lockIngestionRow(itunesId)
      // throw an exception if the locked row already has a dotable, means there was a race
      // condition and this transaciton should be rolled back
      _ = assert(row.podcastDotableId.isEmpty)
      podcastId <- dotableDbIo.insertAndGetId(podcast) if row.podcastDotableId.isEmpty
      _ <- podcastFeedIngestionDbIo.updatePodcastRecordByItunesId(
        podcastId,
        itunesId,
        podcast.feedUrl,
        podcast.feedEtag,
        nextIngestionTime = LocalDateTime.now().plusHours(1))
      episodeIdAndGuids <- dotableDbIo.insertEpisodeBatch(podcastId, episodes)
      _ <- podcastFeedIngestionDbIo.insertEpisodeRecords(podcastId, episodeIdAndGuids)
      _ <- updateTagsForDotable(podcastId, podcast.tags)
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
      newEpisodes = episodesWithExistingId.filter(_._1.isEmpty).map(_._2)
      existingEpisodesWithId = episodesWithExistingId
        .filter(_._1.isDefined)
        .map(pair => pair._1.get -> pair._2)
      _ <- dotableDbIo.updateExisting(podcastId, podcast)
      _ <- podcastFeedIngestionDbIo.updatePodcastRecordByItunesId(
        podcastId,
        itunesId,
        podcast.feedUrl,
        podcast.feedEtag,
        nextIngestionTime = LocalDateTime.now().plusHours(1))
      newEpisodesAndGuids <- dotableDbIo.insertEpisodeBatch(podcastId, newEpisodes)
      _ <- podcastFeedIngestionDbIo.insertEpisodeRecords(podcastId, newEpisodesAndGuids)
      _ <- dotableDbIo.updateEpisodes(podcastId, existingEpisodesWithId)
      _ <- updateTagsForDotable(podcastId, podcast.tags)
    } yield ()).transactionally
    db.run(insertPodcastRowOp).map(_ => podcastId)
  }

  def readLimited(kind: DotableKinds.Value, limit: Int): Future[Seq[Dotable]] = {
    db.run(dotableDbIo.readLimited(kind, limit))
  }

  def search(query: String, kind: DotableKind, limit: Long) = {
    db.run(dotableDbIo.search(query, kind, limit))
  }

  def readTagList(kind: DotableKinds.Value,
                  tagId: model.TagId,
                  limit: Long,
                  personId: Option[Long] = None): Future[Option[model.TagList]] = {
    val tagQuery = for {
      tags <- Tables.Tag.filter(row => row.kind === tagId.kind && row.key === tagId.key)
    } yield tags

    val dotablesQuery = (for {
      tags <- Tables.Tag.filter(row => row.kind === tagId.kind && row.key === tagId.key)
      dotableTags <- Tables.DotableTag if dotableTags.tagId === tags.id
      dotables <- Tables.Dotable if dotables.id === dotableTags.dotableId
    } yield dotables).take(limit)

    val dotesFuture: Future[Seq[Tables.DoteRow]] =
      db.run(
        Tables.Dote
          .filter(row => row.dotableId.in(dotablesQuery.map(_.id)) && row.personId === personId)
          .result)

    db.run(dotablesQuery.result) flatMap { dotables =>
      dotesFuture flatMap { dotes =>
        val combined = setDotes(dotables, dotes)
        if (dotables.nonEmpty) {
          db.run(tagQuery.result.headOption.map(_.map(t =>
            model.TagList(model.Tag(t.kind, t.key, t.name), combined))))
        } else {
          Future(None)
        }
      }
    }
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

  def readDotableShallow(id: Long): Future[Option[Dotable]] = {
    db.run(dotableDbIo.readHeadById(id))
  }

  def getPodcastIngestionRowByItunesId(
      itunesId: Long): Future[Option[Tables.PodcastFeedIngestionRow]] = {
    db.run(podcastFeedIngestionDbIo.readRowByItunesId(itunesId))
  }

  def updateNextIngestionTimeByItunesId(itunesId: Long, nextIngestionTime: LocalDateTime) = {
    db.run(podcastFeedIngestionDbIo.updateNextIngestionTimeByItunesId(itunesId, nextIngestionTime))
  }

  def getNextPodcastIngestionRows(limit: Int): Future[Seq[Tables.PodcastFeedIngestionRow]] = {
    db.run(podcastFeedIngestionDbIo.readNextIngestionRows(limit))
  }

  private def updateTagsForDotable(dotableId: Long, tags: Seq[model.Tag]) = {
    val validatedTags = tags.filter(t => t.id.key.nonEmpty && t.name.nonEmpty)
    DBIO.seq(tagDbIo.upsertTagBatch(validatedTags),
             tagDbIo.upsertDotableTagBatch(dotableId, validatedTags.map(_.id)))
  }

  private def setDotes(dotables: Seq[Tables.DotableRow],
                       dotes: Seq[Tables.DoteRow]): Seq[Dotable] = {
    val dotablesById = dotables.map(d => (d.id, DotableMapper(d)))
    val dotesByDoteableId = dotes.map(d => (d.dotableId, d)).toMap
    dotablesById map {
      case (dotableId, dotable) => {
        val dote = dotesByDoteableId.get(dotableId).map(DoteMapper)
        dotable.copy(dote = dote)
      }
    }
  }

  private def matchToExistingEpisodeId(episodes: Seq[RssFetchedEpisode],
                                       existingEpisodes: Seq[Tables.PodcastEpisodeIngestionRow])
    : Seq[(Option[Long], RssFetchedEpisode)] = {
    val guidMap = existingEpisodes.map(row => row.guid -> row.episodeDotableId).toMap
    episodes.map(episode => guidMap.get(episode.details.rssGuid) -> episode)
  }
}
