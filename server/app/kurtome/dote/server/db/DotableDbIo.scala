package kurtome.dote.server.db

import java.time.{LocalDateTime, ZoneOffset}

import com.trueaccord.scalapb.json.JsonFormat
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.db.dotable._
import kurtome.dote.server.ingestion.{RssFetchedEpisode, RssFetchedPodcast}
import kurtome.dote.server.util.{Slug, UrlIds}
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.slick.db.gen.Tables.DotableRow
import org.json4s.JValue
import kurtome.dote.slick.db.DotePostgresProfile.api._
import javax.inject._
import kurtome.dote.server.util.UrlIds.IdKinds
import kurtome.dote.shared.constants.DotableKinds
import kurtome.dote.shared.constants.DotableKinds.DotableKind
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class DotableDbIo @Inject()(implicit ec: ExecutionContext) extends LogSupport {

  private val table = Tables.Dotable

  val filterRaw = Compiled { (id: Rep[Long]) =>
    table.filter(row => row.id === id)
  }

  def updateExisting(id: Long, podcast: RssFetchedPodcast) = {
    val row = podcastToRow(Some(id), podcast)
    filterRaw(id).update(row)
  }

  def insertAndGetId(podcast: RssFetchedPodcast) = {
    val row = podcastToRow(None, podcast)
    for {
      id <- (table returning table.map(_.id)) += row
    } yield id
  }

  def insertEpisode(podcastId: Long, episode: RssFetchedEpisode) = {
    val row = episodeToRow(None, podcastId, episode)
    for {
      id <- (table returning table.map(_.id)) += row
    } yield (id, episode)
  }

  def insertReview(subjectId: Long, reviewBody: String) = {
    val reviewTime = LocalDateTime.now()

    val data = DotableData()
      .withCommon(
        DotableCommon()
          .withDescription(reviewBody)
          .withUpdatedEpochSec(reviewTime.toEpochSecond(ZoneOffset.UTC))
          .withPublishedEpochSec(reviewTime.toEpochSecond(ZoneOffset.UTC)))

    val row = DotableRow(
      id = 0,
      kind = DotableKinds.Review,
      title = None,
      contentEditedTime = reviewTime,
      parentId = Some(subjectId),
      data = JsonFormat.toJson(data)
    )

    for {
      id <- (table returning table.map(_.id)) += row
    } yield (id)

  }

  def updateReview(reviewId: Long, reviewBody: String) = {
    def updateFields(row: Tables.DotableRow) = {
      val editedTime = LocalDateTime.now()
      val data = JsonFormat.fromJson[DotableData](row.data)
      row.copy(
        contentEditedTime = editedTime,
        data = JsonFormat.toJson(
          data.withCommon(
            data.getCommon
              .withDescription(reviewBody)
              .withUpdatedEpochSec(editedTime.toEpochSecond(ZoneOffset.UTC))))
      )
    }

    for {
      existingRow <- table.filter(_.id === reviewId).result.head
      _ <- table.filter(_.id === reviewId).update(updateFields(existingRow))
    } yield ()
  }

  def updateEpisodes(podcastId: Long, existingEpisodesWithId: Seq[(Long, RssFetchedEpisode)]) = {
    DBIO.sequence(existingEpisodesWithId map {
      case (id, episode) =>
        val row = episodeToRow(Some(id), podcastId, episode)
        filterRaw(id).update(row)
    })
  }

  def maxId() = {
    table.map(_.id).max.result
  }

  val readLimitedRaw = Compiled {
    (kind: Rep[DotableKind], limit: ConstColumn[Long], minId: ConstColumn[Long]) =>
      table
        .filter(row => row.kind === kind && row.id >= minId)
        .take(limit)
  }

  def readLimited(kind: DotableKind, limit: Int, minId: Long = 0) = {
    readLimitedRaw(kind, limit, minId).result.map(_.map(protoRowMapper(kind)))
  }

  def readById(kind: DotableKind, id: Long) = {
    filterRaw(id).result.map(_.map(protoRowMapper(kind)))
  }

  def readHeadById(kind: DotableKind, id: Long) = {
    filterRaw(id).result.headOption.map(_.map(protoRowMapper(kind)))
  }

  def readHeadById(id: Long) = {
    filterRaw(id).result.headOption.map(_.map(protoRowMapper))
  }

  val readByParentIdRaw = Compiled { (parentId: Rep[Long], kind: Rep[DotableKind]) =>
    table
      .filter(row => row.parentId === parentId && row.kind === kind)
      .sortBy(_.contentEditedTime.desc)
  }

  val readByChildIdRaw = Compiled { (childId: Rep[Long]) =>
    for {
      c <- table.filter(_.id === childId)
      (g, p) <- table joinRight table on (_.id === _.parentId)
      if p.id === c.parentId
    } yield (p, g)
  }

  def readHeadByParentId(kind: DotableKind, parentId: Long) = {
    readByParentIdRaw(parentId, kind).result.headOption.map(_.map(protoRowMapper(kind)))
  }

  def readByParentId(kind: DotableKind, parentId: Long) = {
    readByParentIdRaw(parentId, kind).result.map(_.map(protoRowMapper(kind)))
  }

  def readParentAndGrandparent(childId: Long) = {
    readByChildIdRaw(childId).result.headOption map { results =>
      val parent = results.map(_._1)
      val grandparent = results.flatMap(_._2)
      (parent.map(protoRowMapper), grandparent.map(protoRowMapper))
    }
  }

  def readBatchById(ids: Seq[Long]) = {
    (for {
      (d, p) <- Tables.Dotable.filter(_.id inSet ids) joinLeft Tables.Dotable on (_.parentId === _.id)
    } yield (d, p)).result.map(_.map(childAndParent => {
      (protoRowMapper(childAndParent._1), childAndParent._2.map(protoRowMapper))
    }))
  }

  def protoRowMapper(kind: DotableKind)(row: DotableRow): Dotable = {
    assert(row.kind == kind)
    protoRowMapper(row)
  }

  def protoRowMapper(row: DotableRow): Dotable = {
    val kind = row.kind
    val data = JsonFormat.fromJson[DotableData](row.data)
    Dotable(
      id = UrlIds.encode(IdKinds.Dotable, row.id),
      slug = Slug.slugify(data.getCommon.title),
      kind = row.kind match {
        case DotableKinds.Podcast => Dotable.Kind.PODCAST
        case DotableKinds.PodcastEpisode => Dotable.Kind.PODCAST_EPISODE
        case DotableKinds.Review => Dotable.Kind.REVIEW
        case _ => throw new IllegalStateException("unexpected type " + kind)
      },
      common = data.common,
      details = data.details
    )
  }

  private def parseDetails(kind: DotableKind, detailsJson: JValue): DotableDetails.Details = {
    kind match {
      case DotableKinds.Podcast =>
        DotableDetails.Details.Podcast(JsonFormat.fromJson[DotableDetails.Podcast](detailsJson))
      case DotableKinds.PodcastEpisode =>
        DotableDetails.Details
          .PodcastEpisode(JsonFormat.fromJson[DotableDetails.PodcastEpisode](detailsJson))
      case _ => throw new IllegalStateException("unexpected type " + kind)
    }
  }

  private def episodeToRow(id: Option[Long],
                           parentId: Long,
                           ep: RssFetchedEpisode): Tables.DotableRow = {
    val data = DotableData(
      common = Some(ep.common),
      details = Some(DotableDetails(DotableDetails.Details.PodcastEpisode(ep.details))))
    DotableRow(
      id = id.getOrElse(0),
      kind = DotableKinds.PodcastEpisode,
      title = Some(ep.common.title),
      contentEditedTime = LocalDateTime.ofEpochSecond(ep.common.updatedEpochSec, 0, ZoneOffset.UTC),
      parentId = Some(parentId),
      data = JsonFormat.toJson(data)
    )
  }

  private def podcastToRow(id: Option[Long], podcast: RssFetchedPodcast): Tables.DotableRow = {
    val data = DotableData(common = Some(podcast.common),
                           details =
                             Some(DotableDetails(DotableDetails.Details.Podcast(podcast.details))))
    DotableRow(
      id = id.getOrElse(0),
      kind = DotableKinds.Podcast,
      title = Some(podcast.common.title),
      contentEditedTime =
        LocalDateTime.ofEpochSecond(podcast.common.updatedEpochSec, 0, ZoneOffset.UTC),
      data = JsonFormat.toJson(data),
      parentId = None
    )
  }

  val ilikeSearchRaw = Compiled {
    (query: ConstColumn[String], kind: ConstColumn[DotableKind], limit: ConstColumn[Long]) =>
      for {
        (d, p) <- table
          .filter(row => (row.title ilike query) && row.kind === kind)
          .take(limit) joinLeft table on (_.parentId === _.id)
      } yield (d, p)
  }

  val fuzzySearchRaw = Compiled {
    (psqlQueryStr: ConstColumn[String],
     kind: ConstColumn[DotableKind],
     limit: ConstColumn[Long]) =>
      // Use full text search on the title column and order by the highest text ranking
      for {
        (d, p) <- table
          .filter(row => {
            toTsVector(row.title, Some("english")) @@ toTsQuery(psqlQueryStr, Some("english")) && row.kind === kind
          })
          .map(row =>
            (row,
             tsRank(toTsVector(row.title, Some("english")),
                    toTsQuery(psqlQueryStr, Some("english")))))
          .sortBy(rowTup => rowTup._2.desc)
          .take(limit)
          .map(_._1) joinLeft table on (_.parentId === _.id)
      } yield (d, p)
  }

  def search(query: String, kind: DotableKind, limit: Long) = {
    val parts = query.split("\\W")
    // join on '&' to logically and the words together
    // add ':*' to the last word, assuming it may be a partial word
    val fuzzyQueryStr = if (query.nonEmpty) parts.mkString("&") else ""

    (for {
      fuzzyResults <- fuzzySearchRaw(fuzzyQueryStr, kind, limit).result
      prefixResults <- ilikeSearchRaw(query + "%", kind, limit).result
      //prefixResults <- prefixSearchRaw(query + "%", kind, limit).result
      prefixIds = prefixResults.map(_._1.id).toSet
      // put prefix results before fuzzy results and remove duplicates
      results = prefixResults ++ fuzzyResults.filterNot(pair => prefixIds.contains(pair._1.id))
    } yield results).map(_ map {
      case (result, None) => protoRowMapper(kind)(result)
      case (result, Some(parent)) => {
        val relatives =
          Dotable.Relatives(parentFetched = true, parent = Some(protoRowMapper(parent)))
        protoRowMapper(kind)(result).withRelatives(relatives)
      }
    })
  }

  /**
    * Returns a batch of IDs with db_updated_time > cutOffAge, and guaranteeing the batch contains
    * all dotables with db_updated_time = max(db_updated_time) of this batch. In other words, it
    * is safe to use the max db_updated time of this batch as the next cutOffAge.
    */
  def nextOldestModifiedBatch(approxBatchSize: Int, cutOffAge: LocalDateTime) = {
    val cutOffTimestamp: java.sql.Timestamp = java.sql.Timestamp.valueOf(cutOffAge)
    sql"""
         SELECT d1.id, d1.db_updated_time
         FROM dotable d1
         WHERE d1.db_updated_time > $cutOffTimestamp AND d1.db_updated_time <= (
           SELECT MAX(temp.db_updated_time) FROM (
             SELECT d2.db_updated_time as db_updated_time
             FROM dotable d2
             WHERE d2.db_updated_time > $cutOffTimestamp
             ORDER BY d2.db_updated_time
             LIMIT $approxBatchSize) AS temp
           )
       """
      .as[(Long, java.sql.Timestamp)]
      .map(_.map(row => {
        (row._1, row._2.toLocalDateTime)
      }))
  }
}
