package kurtome.dote.server.db

import java.time.{LocalDateTime, ZoneOffset}
import javax.inject._

import com.trueaccord.scalapb.json.JsonFormat
import dote.proto.api.dotable.Dotable
import dote.proto.db.dotable.{DotableCommon, DotableDetails}
import kurtome.dote.server.controllers.podcast.{RssFetchedEpisode, RssFetchedPodcast}
import kurtome.dote.server.util.UrlIds
import kurtome.dote.slick.db.DotableKinds
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.slick.db.gen.Tables.DotableRow
import org.json4s.JValue
import slick.basic.BasicBackend
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Database @Inject()(db: BasicBackend#DatabaseDef)(implicit ec: ExecutionContext) {

  def ingestPodcast(podcast: RssFetchedPodcast): Future[Dotable] = {
    val insertPodcastRowOp = (for {
      rowWithId <- (Tables.Dotable returning Tables.Dotable.map(_.id) into (
          (podcastRow,
           id) => podcastRow.copy(id = id))) +=
        Tables.DotableRow(
          id = 0,
          kind = DotableKinds.Podcast,
          title = Some(podcast.common.title),
          description = Some(podcast.common.description),
          publishedTime =
            LocalDateTime.ofEpochSecond(podcast.common.publishedEpochSec, 0, ZoneOffset.UTC),
          editedTime =
            LocalDateTime.ofEpochSecond(podcast.common.updatedEpochSec, 0, ZoneOffset.UTC),
          parentId = None,
          common = JsonFormat.toJson(podcast.common),
          details = JsonFormat.toJson(podcast.details),
          // These should be set by the DB
          dbCreatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC),
          dbUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
        )
      episodes = podcast.episodes.map(episodeToRow(rowWithId.id, _))
      insertedNum <- Tables.Dotable ++= episodes
    } yield (rowWithId, episodes)).transactionally

    db.run(insertPodcastRowOp) map {
      case (podcastRow, episodeRows) =>
        podcastRowMapper(podcastRow, episodeRows)
    }
//    andThen {
//      case Success(_) => {
//        val podcast = for {
//          (podcast, episodes) <- Tables.Dotable.filter(_.id === id) join Tables.Dotable on (_.id === _.parentId)
//        } yield (podcast, episodes)
//      }
//      case Failure(t) => Failure(t)
//    }
  }

  private def podcastRowMapper(row: DotableRow, episodeRows: Seq[DotableRow]): Dotable = {
    assert(row.kind == DotableKinds.Podcast)
    val episodes: Seq[Dotable] = episodeRows.map(episodeRowMapper)
    Dotable(
      id = UrlIds.encode(row.id),
      kind = Dotable.Kind.PODCAST,
      common = Some(JsonFormat.fromJson[DotableCommon](row.common)),
      details = Some(Dotable.Details(detailsFetched = true, details = parseDetails(row.details))),
      relatives = Some(Dotable.Relatives(children = episodes, childrenFetched = true))
    )
  }

  private def episodeRowMapper(row: DotableRow): Dotable = {
    assert(row.kind == DotableKinds.PodcastEpisode)
    Dotable(
      id = UrlIds.encode(row.id),
      kind = Dotable.Kind.PODCAST_EPISODE,
      common = Some(JsonFormat.fromJson[DotableCommon](row.common)),
      details = Some(Dotable.Details(detailsFetched = true, details = parseDetails(row.details)))
    )
  }

  private def parseDetails(detailsJson: JValue): Dotable.Details.Details = {
    Dotable.Details.Details.Podcast(JsonFormat.fromJson[DotableDetails.Podcast](detailsJson))
  }

  private def episodeToRow(parentId: Long, ep: RssFetchedEpisode): Tables.DotableRow = {
    Tables.DotableRow(
      id = 0,
      kind = DotableKinds.PodcastEpisode,
      title = Some(ep.common.title),
      description = Some(ep.common.description),
      publishedTime = LocalDateTime.ofEpochSecond(ep.common.publishedEpochSec, 0, ZoneOffset.UTC),
      editedTime = LocalDateTime.ofEpochSecond(ep.common.updatedEpochSec, 0, ZoneOffset.UTC),
      parentId = Some(parentId),
      common = JsonFormat.toJson(ep.common),
      details = JsonFormat.toJson(ep.details),
      // These should be set by the DB
      dbCreatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC),
      dbUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
    )
  }
}
