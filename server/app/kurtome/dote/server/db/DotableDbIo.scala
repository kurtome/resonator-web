package kurtome.dote.server.db

import java.time.{LocalDateTime, ZoneOffset}

import com.trueaccord.scalapb.json.JsonFormat
import dote.proto.api.dotable.Dotable
import dote.proto.db.dotable._
import kurtome.dote.server.ingestion.{RssFetchedEpisode, RssFetchedPodcast}
import kurtome.dote.server.util.{Slug, UrlIds}
import kurtome.dote.slick.db.DotableKinds
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.slick.db.gen.Tables.DotableRow
import org.json4s.JValue
import kurtome.dote.slick.db.DotePostgresProfile.api._
import javax.inject._

import kurtome.dote.server.util.UrlIds.IdKinds
import kurtome.dote.slick.db.DotableKinds.DotableKind

import scala.concurrent.ExecutionContext

@Singleton
class DotableDbIo @Inject()(implicit ec: ExecutionContext) {

  private val table = Tables.Dotable

  def updateExisting(id: Long, podcast: RssFetchedPodcast) = {
    val row = podcastToRow(Some(id), podcast)
    table.filter(row => row.id === id && row.kind === DotableKinds.Podcast).update(row)
  }

  def insertAndGetId(podcast: RssFetchedPodcast) = {
    val row = podcastToRow(None, podcast)
    for {
      id <- (table returning table.map(_.id)) += row
    } yield (id)
  }

  def insertEpisodeBatch(podcastId: Long, episodes: Seq[RssFetchedEpisode]) = {
    DBIO.sequence(for {
      episode <- episodes
      row = episodeToRow(None, podcastId, episode)
      pairs = for {
        id <- (table returning table.map(_.id)) += row
      } yield (id, episode.details.rssGuid)
    } yield pairs)
  }

  def updateEpisodes(podcastId: Long, existingEpisodesWithId: Seq[(Long, RssFetchedEpisode)]) = {
    DBIO.sequence(existingEpisodesWithId map {
      case (id, episode) =>
        val row = episodeToRow(Some(id), podcastId, episode)
        table.filter(row => row.id === id && row.kind === DotableKinds.PodcastEpisode).update(row)
    })
  }

  def maxId() = {
    table.map(_.id).max.result
  }

  def readLimited(kind: DotableKind, limit: Int, minId: Long = 0) = {
    table
      .filter(row => row.kind === kind && row.id >= minId)
      .take(limit)
      .result
      .map(_.map(protoRowMapper(kind)))
  }

  val readByIdRaw = Compiled { (id: Rep[Long]) =>
    table.filter(_.id === id)
  }

  def readById(kind: DotableKind, id: Long) = {
    readByIdRaw(id).result.map(_.map(protoRowMapper(kind)))
  }

  def readHeadById(kind: DotableKind, id: Long) = {
    readByIdRaw(id).result.headOption.map(_.map(protoRowMapper(kind)))
  }

  def readHeadById(id: Long) = {
    readByIdRaw(id).result.headOption.map(_.map(protoRowMapper))
  }

  val readByParentIdRaw = Compiled { (parentId: Rep[Long]) =>
    table.filter(_.parentId === parentId)
  }

  val readByChildIdRaw = Compiled { (childId: Rep[Long]) =>
    for {
      (p, c) <- table join table.filter(_.id === childId) on (_.id === _.parentId)
    } yield p
  }

  def readHeadByParentId(kind: DotableKind, parentId: Long) = {
    readByParentIdRaw(parentId).result.headOption.map(_.map(protoRowMapper(kind)))
  }

  def readByParentId(kind: DotableKind, parentId: Long) = {
    readByParentIdRaw(parentId).result.map(_.map(protoRowMapper(kind)))
  }

  def readByChildId(kind: DotableKind, childId: Long) = {
    readByChildIdRaw(childId).result.headOption.map(_.map(protoRowMapper(kind)))
  }

  def readByParentId(parentId: Long) = {
    readByParentIdRaw(parentId).result.map(_.map(protoRowMapper))
  }

  def readByIdBatch(kind: DotableKind, ids: Set[Long]) = {
    table
      .filter(row => row.id.inSet(ids) && row.kind === kind)
      .result
      .map(_.map(protoRowMapper(kind)))
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

  def search(query: String, kind: DotableKind, limit: Long) = {
    val parts = query.split("\\W")
    // join on '|' to logically or the words together
    // add ':*' to the last word, assuming it may be a partial word
    val psqlQueryStr = if (query.nonEmpty) parts.mkString("&") + ":*" else ""
    // Use full text search on the title column and order by the highest text ranking
    table
      .filter(row => {
        toTsVector(row.title, Some("english")) @@ toTsQuery(psqlQueryStr) && row.kind === kind
      })
      .map(row => (row, tsRank(toTsVector(row.title, Some("english")), toTsQuery(psqlQueryStr))))
      .sortBy(_._2.desc)
      .take(limit)
      .map(_._1)
      .result
      .map(_.map(protoRowMapper(kind)))
  }
}
