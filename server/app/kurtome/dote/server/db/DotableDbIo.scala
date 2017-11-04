package kurtome.dote.server.db

import java.time.{LocalDateTime, ZoneOffset}

import com.trueaccord.scalapb.json.JsonFormat
import dote.proto.api.dotable.Dotable
import dote.proto.db.dotable.{DotableCommon, DotableDetails}
import kurtome.dote.server.controllers.podcast.{RssFetchedEpisode, RssFetchedPodcast}
import kurtome.dote.server.util.{Slug, UrlIds}
import kurtome.dote.slick.db.DotableKinds
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.slick.db.gen.Tables.DotableRow
import org.json4s.JValue
import kurtome.dote.slick.db.DotePostgresProfile.api._
import javax.inject._

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
         id <- (table returning table.map (_.id)) += row
        } yield (id, episode.details.rssGuid)
      } yield pairs)
  }

  def updateEpisodes(podcastId: Long, existingEpisodesWithId: Seq[(Long, RssFetchedEpisode)]) = {
    DBIO.sequence(existingEpisodesWithId map { case (id, episode) =>
      val row = episodeToRow(Some(id), podcastId, episode)
      table.filter(row => row.id === id && row.kind === DotableKinds.PodcastEpisode).update(row)
    })
  }

  def readLimited(kind: DotableKinds.Value, limit: Int) = {
    table.filter(_.kind === kind).take(limit).result.map(_.map(protoRowMapper(kind)))
  }

  val readByIdRaw = Compiled { (kind: Rep[DotableKinds.Value], id: Rep[Long]) =>
    table.filter(_.id === id)
  }

  def readById(kind: DotableKinds.Value, id: Long) = {
    readByIdRaw(kind, id).result.map(_.map(protoRowMapper(kind)))
  }

  def readHeadById(kind: DotableKinds.Value, id: Long) = {
    readByIdRaw(kind, id).result.headOption.map(_.map(protoRowMapper(kind)))
  }

  val readByParentIdRaw = Compiled { (kind: Rep[DotableKinds.Value], parentId: Rep[Long]) =>
    table.filter(_.parentId === parentId)
  }

  def readHeadByParentId(kind: DotableKinds.Value, parentId: Long) = {
    readByParentIdRaw(kind, parentId).result.headOption.map(_.map(protoRowMapper(kind)))
  }

  def readByParentId(kind: DotableKinds.Value, parentId: Long) = {
    readByParentIdRaw(kind, parentId).result.map(_.map(protoRowMapper(kind)))
  }

  def protoRowMapper(kind: DotableKinds.Value)(row: DotableRow): Dotable = {
    assert(row.kind == kind)
    val common = JsonFormat.fromJson[DotableCommon](row.common)
    Dotable(
      id = UrlIds.encode(row.id),
      slug = Slug.slugify(common.title),
      kind = row.kind match {
        case DotableKinds.Podcast => Dotable.Kind.PODCAST
        case DotableKinds.PodcastEpisode => Dotable.Kind.PODCAST_EPISODE
      },
      common = Some(common),
      details = Some(Dotable.Details(detailsFetched = true, details = parseDetails(row.details))),
    )
  }

  private def parseDetails(detailsJson: JValue): Dotable.Details.Details = {
    Dotable.Details.Details.Podcast(JsonFormat.fromJson[DotableDetails.Podcast](detailsJson))
  }

  private def episodeToRow(id: Option[Long], parentId: Long, ep: RssFetchedEpisode): Tables.DotableRow = {
    Tables.DotableRow(
      id = id.getOrElse(0),
      kind = DotableKinds.PodcastEpisode,
      title = Some(ep.common.title),
      description = Some(ep.common.description),
      publishedTime = LocalDateTime.ofEpochSecond(ep.common.publishedEpochSec, 0, ZoneOffset.UTC),
      editedTime = LocalDateTime.ofEpochSecond(ep.common.updatedEpochSec, 0, ZoneOffset.UTC),
      parentId = Some(parentId),
      common = JsonFormat.toJson(ep.common),
      details = JsonFormat.toJson(ep.details)
    )
  }
  private def podcastToRow(id: Option[Long], podcast: RssFetchedPodcast): Tables.DotableRow = {
    Tables.DotableRow(
      id = id.getOrElse(0),
      kind = DotableKinds.Podcast,
      title = Some(podcast.common.title),
      description = Some(podcast.common.description),
      publishedTime =
        LocalDateTime.ofEpochSecond(podcast.common.publishedEpochSec, 0, ZoneOffset.UTC),
      editedTime = LocalDateTime.ofEpochSecond(podcast.common.updatedEpochSec, 0, ZoneOffset.UTC),
      parentId = None,
      common = JsonFormat.toJson(podcast.common),
      details = JsonFormat.toJson(podcast.details)
    )
  }
}
