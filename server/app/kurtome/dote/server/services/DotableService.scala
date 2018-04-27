package kurtome.dote.server.services

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

import javax.inject._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.tag.TagCollection
import kurtome.dote.server.db._
import kurtome.dote.server.db.DotableDbIo
import kurtome.dote.server.db.mappers.{DotableMapper, DoteMapper}
import kurtome.dote.server.ingestion.{RssFetchedEpisode, RssFetchedPodcast}
import kurtome.dote.server.model.MetadataFlag
import kurtome.dote.shared.constants.DotableKinds
import kurtome.dote.shared.constants.DotableKinds.DotableKind
import kurtome.dote.shared.model
import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.shared.constants.TagKinds
import kurtome.dote.shared.constants.TagKinds.TagKind
import kurtome.dote.shared.mapper.TagMapper
import kurtome.dote.shared.model.PaginationInfo
import kurtome.dote.shared.model.Tag
import kurtome.dote.shared.model.TagId
import kurtome.dote.shared.model.TagList
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.slick.db.gen.Tables.PodcastEpisodeIngestionRow
import slick.basic.BasicBackend
import wvlet.log.LogSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class DotableService @Inject()(db: BasicBackend#Database,
                               dotableDbIo: DotableDbIo,
                               doteService: DoteService,
                               personService: PersonService,
                               podcastFeedIngestionDbIo: PodcastFeedIngestionDbIo,
                               tagDbIo: DotableTagDbIo)(implicit ec: ExecutionContext)
    extends LogSupport {

  object Queries {
    val tagList = Compiled {
      (kind: Rep[DotableKind],
       tagKind: Rep[TagKind],
       tagKey: Rep[String],
       offset: ConstColumn[Long],
       limit: ConstColumn[Long]) =>
        (for {
          tags <- Tables.Tag.filter(row => row.kind === tagKind && row.key === tagKey)
          dotableTags <- Tables.DotableTag if dotableTags.tagId === tags.id
          (dotables, parents) <- Tables.Dotable joinLeft Tables.Dotable on (_.parentId === _.id)
          if dotables.id === dotableTags.dotableId && dotables.kind === kind
        } yield (dotables, parents)).sortBy(_._1.contentEditedTime.desc).drop(offset).take(limit)
    }

    val personTagListDotes = Compiled {
      (kind: Rep[DotableKind],
       tagKind: Rep[TagKind],
       tagKey: Rep[String],
       offset: ConstColumn[Long],
       listLimit: ConstColumn[Long],
       personId: Rep[Long]) =>
        val dotablesQuery = (for {
          tags <- Tables.Tag.filter(row => row.kind === tagKind && row.key === tagKey)
          dotableTags <- Tables.DotableTag if dotableTags.tagId === tags.id
          dotables <- Tables.Dotable
          if dotables.id === dotableTags.dotableId && dotables.kind === kind
        } yield dotables).sortBy(_.contentEditedTime.desc).drop(offset).take(listLimit)

        for {
          dotables <- dotablesQuery
          dotes <- Tables.Dote if dotes.dotableId === dotables.id && dotes.personId === personId
        } yield dotes
    }

    val recentEpisodesFromPodcastTagList = Compiled {
      (tagKind: Rep[TagKind],
       tagKey: Rep[String],
       offset: ConstColumn[Long],
       limit: ConstColumn[Long]) =>
        (for {
          tags <- Tables.Tag.filter(row => row.kind === tagKind && row.key === tagKey)
          dotableTags <- Tables.DotableTag if dotableTags.tagId === tags.id
          podcasts <- Tables.Dotable
          if podcasts.id === dotableTags.dotableId && podcasts.kind === DotableKinds.Podcast
          episodes <- Tables.Dotable
          if episodes.parentId === podcasts.id && episodes.kind === DotableKinds.PodcastEpisode
        } yield (episodes, podcasts))
          .sortBy(_._1.contentEditedTime.desc)
          .drop(offset)
          .take(limit)
    }

  }

  lazy val popularTagDbId: Future[Long] =
    db.run(tagDbIo.readTagDbId(MetadataFlag.Ids.popular)).map(_.get)

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
    db.run((for {
      _ <- podcastFeedIngestionDbIo.insertIfNew(itunesId, podcast.feedUrl)
      row <- podcastFeedIngestionDbIo.lockIngestionRow(itunesId)
      // throw an exception if the locked row already has a dotable, means there was a race
      // condition and this transaciton should be rolled back
      _ = assert(row.podcastDotableId.isEmpty)
      podcastId <- dotableDbIo.insertAndGetId(podcast) if row.podcastDotableId.isEmpty
    } yield podcastId).transactionally) flatMap { podcastId =>
      val waitMinutes = calcIngestionWaitMinutes(podcast)

      val episodeInserts = episodes map { episode =>
        (for {
          // new episodes and ingestion records must be part of one transaction to prevent duplicates.
          // (podcast_episode_ingestion table has unique keys to prevent this)
          idAndGuid <- dotableDbIo.insertEpisode(podcastId, episode)
          _ <- podcastFeedIngestionDbIo.insertEpisodeRecord(podcastId, idAndGuid._1, idAndGuid._2)
        } yield ()).transactionally
      }

      db.run(
          DBIO
            .seq(
              DBIO.sequence(episodeInserts),
              updateTagsForDotable(podcastId, podcast.tags),
              podcastFeedIngestionDbIo.updatePodcastRecordByItunesId(podcastId,
                                                                     itunesId,
                                                                     podcast.feedUrl,
                                                                     podcast.feedEtag,
                                                                     podcast.dataHash,
                                                                     reingestWaitMinutes =
                                                                       waitMinutes)
            )
            .withStatementParameters(statementInit = _.setQueryTimeout(30)))
        .map(_ => podcastId)
    }
  }

  private def ingestExistingPodcast(itunesId: Long,
                                    podcast: RssFetchedPodcast,
                                    episodes: Seq[RssFetchedEpisode],
                                    podcastId: Long) = {

    // Podcast already exists, update it

    db.run(podcastFeedIngestionDbIo.readEpisodesByPodcastId(podcastId)) flatMap {
      existingEpisodes =>
        val episodesWithExistingId =
          matchToExistingEpisodeIdAndFilterUnchanged(episodes, existingEpisodes)
        val newEpisodes = episodesWithExistingId.filter(_._1.isEmpty).map(_._2)
        val existingEpisodesWithId = episodesWithExistingId
          .filter(_._1.isDefined)
          .map(pair => pair._1.get -> pair._2)

        val waitMinutes = calcIngestionWaitMinutes(podcast)

        val episodeInserts = newEpisodes map { episode =>
          (for {
            // new episodes and ingestion records must be part of one transaction to prevent duplicates.
            // (podcast_episode_ingestion table has unique keys to prevent this)
            idAndGuid <- dotableDbIo.insertEpisode(podcastId, episode)
            _ <- podcastFeedIngestionDbIo.insertEpisodeRecord(podcastId,
                                                              idAndGuid._1,
                                                              idAndGuid._2)
          } yield ()).transactionally
        }

        val updateOperations = DBIO
          .seq(
            dotableDbIo.updateExisting(podcastId, podcast),
            DBIO.sequence(episodeInserts),
            podcastFeedIngestionDbIo.updateEpisodeRecords(podcastId, existingEpisodesWithId),
            dotableDbIo.updateEpisodes(podcastId, existingEpisodesWithId),
            updateTagsForDotable(podcastId, podcast.tags),
            podcastFeedIngestionDbIo.updatePodcastRecordByItunesId(podcastId,
                                                                   itunesId,
                                                                   podcast.feedUrl,
                                                                   podcast.feedEtag,
                                                                   podcast.dataHash,
                                                                   reingestWaitMinutes =
                                                                     waitMinutes)
          )

        db.run(updateOperations.withStatementParameters(statementInit = _.setQueryTimeout(30)))
          .map(_ => podcastId)
    }

  }

  def readLimited(kind: DotableKinds.Value, limit: Int): Future[Seq[Dotable]] = {
    db.run(dotableDbIo.readLimited(kind, limit))
  }

  def search(query: String, kind: DotableKind, limit: Long) = {
    db.run(dotableDbIo.search(query, kind, limit))
  }

  def readPodcastTagList(kind: DotableKinds.Value,
                         tagId: TagId,
                         paginationInfo: PaginationInfo,
                         person: Option[Tables.PersonRow] = None): Future[TagList] = {
    val tagQuery = for {
      tags <- Tables.Tag.filter(row => row.kind === tagId.kind && row.key === tagId.key)
    } yield tags

    val dotablesQuery =
      Queries.tagList(kind, tagId.kind, tagId.key, paginationInfo.offset, paginationInfo.pageSize)

    val dotesFuture: Future[Seq[Tables.DoteRow]] = if (person.isDefined) {
      db.run(
        Queries
          .personTagListDotes(kind,
                              tagId.kind,
                              tagId.key,
                              paginationInfo.offset,
                              paginationInfo.pageSize,
                              person.get.id)
          .result)
    } else {
      Future(Nil)
    }

    db.run(dotablesQuery.result) flatMap { dotables =>
      dotesFuture flatMap { dotes =>
        val combined = setDotes(dotables, dotes, person)
        db.run(tagQuery.result.headOption.map(t =>
          model.TagList(Tag(t.get.kind, t.get.key, t.get.name), combined)))
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
    } yield
      podcast.map(
        _.update(_.relatives := Dotable.Relatives(children = episodes, childrenFetched = true)))
    db.run(op)
  }

  def readDotableDetails(id: Long, personId: Option[Long]): Future[Option[Dotable]] = {
    val childrenQuery = dotableDbIo.readByParentId(DotableKinds.PodcastEpisode, id)
    val parentQuery = dotableDbIo.readParentAndGrandparent(id)
    val tagsQuery = tagDbIo.readByDotableId(id)

    val op = for {
      dotableOpt <- dotableDbIo.readHeadById(id)
      children <- childrenQuery
      (parentOpt, grandParentOpt) <- parentQuery
      tags <- tagsQuery
    } yield
      dotableOpt.map(
        _.withRelatives(Dotable.Relatives(
          parent = parentOpt.map(
            _.withRelatives(Dotable.Relatives(parent = grandParentOpt, parentFetched = true))),
          parentFetched = true,
          children = children,
          childrenFetched = true
        )).withTagCollection(TagCollection(tagsFetched = true, tags = tags map { tagRow =>
          TagMapper.toProto(
            Tag(
              kind = tagRow.kind,
              key = tagRow.key,
              name = tagRow.name
            ))
        })))
    for {
      dotable <- db.run(op)
      dote <- doteService.readDote(personId.getOrElse(0), id)
      person <- personService.readById(personId.getOrElse(0))
      extras <- getExtras(id, dotable.map(_.kind))
    } yield dotable.map(_.copy(dote = dote.map(DoteMapper.toProto(_, person))).withExtras(extras))
  }

  private def getExtras(id: Long, kind: Option[Dotable.Kind]): Future[Dotable.Extras] = {
    kind match {
      case Some(Dotable.Kind.REVIEW) => {
        val pendingDote = for {
          reviewDote <- doteService.doteForReview(id)
          person <- personService.readById(reviewDote.map(_.personId).getOrElse(0))
        } yield reviewDote.map(DoteMapper.toProto(_, person))

        pendingDote.map(opt => {
          Dotable.Extras.Review(Dotable.ReviewExtras(opt))
        })
      }
      case _ => Future(Dotable.Extras.Empty)
    }
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

  def readEpisodeTagList(tagId: TagId, paginationInfo: PaginationInfo): Future[TagList] = {
    val q = Queries
      .recentEpisodesFromPodcastTagList(tagId.kind,
                                        tagId.key,
                                        paginationInfo.offset,
                                        paginationInfo.pageSize)
      .result
      .map(list =>
        list map {
          case (d, parent) => DotableMapper(d, Some(parent))
      })
    for {
      list <- db.run(q)
      tag <- db.run(tagDbIo.readTagById(tagId))
    } yield TagList(tag.get, list)
  }

  def replaceMetadataTagList(tag: MetadataFlag.Keys.Value, dotableIds: Seq[Long]) = {
    db.run(
      Tables.Tag
        .filter(row => row.kind === TagKinds.MetadataFlag && row.key === tag.toString)
        .result
        .headOption) map {
      case Some(dbTag) => {
        val newTagRows = dotableIds map { dotableId =>
          Tables.DotableTagRow(tagId = dbTag.id, dotableId = dotableId)
        }
        val replaceQuery = (for {
          deleteCount <- Tables.DotableTag.filter(_.tagId === dbTag.id).delete
          insertCount <- Tables.DotableTag ++= newTagRows
        } yield deleteCount).transactionally
        db.run(replaceQuery)
      }
      case None => throw new IllegalStateException(s"Couldn't find tag for '$tag'")
    }
  }

  def readAllPodcastIds(): Future[Seq[Long]] = {
    db.run(podcastFeedIngestionDbIo.readAllPodcastDotableIds())
  }

  def readNextOldestModifiedBatch(approxBatchSize: Int,
                                  cutOffAge: LocalDateTime): Future[Seq[(Long, LocalDateTime)]] = {
    db.run(dotableDbIo.nextOldestModifiedBatch(approxBatchSize, cutOffAge))
  }

  def readBatchById(ids: Seq[Long]): Future[Seq[Dotable]] = {
    db.run(dotableDbIo.readBatchById(ids))
      .map(_.map {
        case (dotable, parent) => {
          dotable.withRelatives(Dotable.Relatives(parentFetched = true, parent = parent))
        }
      })
  }

  private def updateTagsForDotable(dotableId: Long, tags: Seq[model.Tag]) = {
    val validatedTags = tags.filter(t => t.id.key.nonEmpty && t.name.nonEmpty)
    DBIO.seq(tagDbIo.upsertTagBatch(validatedTags),
             tagDbIo.upsertDotableTagBatch(dotableId, validatedTags.map(_.id)))
  }

  private def setDotes(dotables: Seq[(Tables.DotableRow, Option[Tables.DotableRow])],
                       dotes: Seq[Tables.DoteRow],
                       person: Option[Tables.PersonRow]): Seq[Dotable] = {
    val dotablesById = dotables map {
      case (d, parent) => (d.id, DotableMapper(d, parent))
    }
    val dotesByDoteableId = dotes.map(d => (d.dotableId, d)).toMap
    dotablesById map {
      case (dotableId, dotable) => {
        val dote = dotesByDoteableId.get(dotableId).map(DoteMapper.toProto(_, person))
        dotable.copy(dote = dote)
      }
    }
  }

  private def matchToExistingEpisodeIdAndFilterUnchanged(
      episodes: Seq[RssFetchedEpisode],
      existingEpisodes: Seq[PodcastEpisodeIngestionRow])
    : Seq[(Option[Long], RssFetchedEpisode)] = {
    val guidMap = existingEpisodes.map(row => row.guid -> row).toMap
    episodes
      .map(episode => guidMap.get(episode.details.rssGuid) -> episode)
      .filter(shouldReingestEpisode)
      .map(pair => (pair._1.map(_.episodeDotableId), pair._2))
  }

  //noinspection MapGetOrElseBoolean
  private def shouldReingestEpisode(
      pair: (Option[PodcastEpisodeIngestionRow], RssFetchedEpisode)): Boolean = {
    pair._1.flatMap(_.lastDataHash) map { existingDataHash =>
      val same = existingDataHash sameElements pair._2.dataHash
      !same
    } getOrElse true // always ingest new episodes
  }

  private def calcIngestionWaitMinutes(podcast: RssFetchedPodcast): Long = {
    val lastEpisodeTime =
      LocalDateTime.ofEpochSecond(podcast.common.updatedEpochSec, 0, ZoneOffset.UTC)
    val now = LocalDateTime.now()

    val duration = if (lastEpisodeTime.isAfter(now.minusWeeks(1))) {
      // Less than a week old
      Duration.ofHours(1)
    } else if (lastEpisodeTime.isAfter(now.minusMonths(1))) {
      // Less than a month old
      Duration.ofHours(6)
    } else if (lastEpisodeTime.isAfter(now.minusYears(1))) {
      // Less than a year old
      Duration.ofDays(1)
    } else {
      // Over a year old
      Duration.ofDays(7)
    }

    duration.getSeconds / 60
  }
}
