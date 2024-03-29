package resonator.server.db

import java.time.LocalDateTime

import javax.inject._
import resonator.server.ingestion.RssFetchedEpisode
import resonator.slick.db.DotePostgresProfile.api._
import resonator.slick.db.gen.Tables

import scala.concurrent.ExecutionContext

@Singleton
class PodcastFeedIngestionDbIo @Inject()(implicit ec: ExecutionContext) {

  private val feedTable = Tables.PodcastFeedIngestion
  private val episodeTable = Tables.PodcastEpisodeIngestion

  object PodcastQueries {
    val allPodcastDotableIds = Compiled {
      for {
        row <- feedTable.filter(_.podcastDotableId.isDefined)
      } yield row.podcastDotableId.get
    }
  }

  object EpisodeQueries {
    val selectDataHashByDotableId = Compiled { (episodeDotableId: Rep[Long]) =>
      for {
        row <- episodeTable.filter(_.episodeDotableId === episodeDotableId)
      } yield row.lastDataHash
    }
  }

  def insertEpisodeRecord(podcastId: Long, episodeId: Long, episode: RssFetchedEpisode) = {
    episodeTable += Tables.PodcastEpisodeIngestionRow(
      id = 0,
      podcastDotableId = podcastId,
      guid = episode.details.rssGuid,
      episodeDotableId = episodeId,
      lastDataHash = Some(episode.dataHash),
      dbUpdatedTime = LocalDateTime.MIN
    )
  }

  def updateEpisodeRecords(podcastId: Long,
                           episodeIdAndFetchedData: Seq[(Long, RssFetchedEpisode)]) = {
    DBIO.sequence(episodeIdAndFetchedData map {
      case (episodeId, fetchedEpisode) =>
        EpisodeQueries.selectDataHashByDotableId(episodeId).update(Some(fetchedEpisode.dataHash))
    })
  }

  def insertIfNew(itunesId: Long, feedUrl: String) = {
    sqlu"""INSERT INTO podcast_feed_ingestion (itunes_id, feed_rss_url)
         SELECT ${itunesId}, ${feedUrl}
         WHERE NOT EXISTS (
         SELECT 1 FROM podcast_feed_ingestion pfi WHERE pfi.itunes_id = ${itunesId})"""
  }

  def lockIngestionRow(itunesId: Long) = {
    feedTable.filter(_.itunesId === itunesId).forUpdate.result.headOption.map(_.get)
  }

  def updatePodcastRecordByItunesId(podcastDotableId: Long,
                                    itunesId: Long,
                                    feedUrl: String,
                                    feedEtag: Option[String],
                                    dataHash: Array[Byte],
                                    reingestWaitMinutes: Long) = {
    val q = for {
      row <- feedTable.filter(_.itunesId === itunesId)
    } yield
      (row.podcastDotableId,
       row.feedRssUrl,
       row.lastFeedEtag,
       row.lastDataHash,
       row.reingestWaitMinutes,
       row.nextIngestionTime)
    q.update(
      (Some(podcastDotableId),
       feedUrl,
       feedEtag,
       Some(dataHash),
       reingestWaitMinutes,
       LocalDateTime.now().plusMinutes(reingestWaitMinutes)))
  }

  def updateNextIngestionTimeByItunesId(itunesId: Long, nextIngestionTime: LocalDateTime) = {
    feedTable.filter(_.itunesId === itunesId).map(_.nextIngestionTime).update(nextIngestionTime)
  }

  val readRssFeedUrlByItunesIdRaw = Compiled { (itunesId: Rep[Long]) =>
    feedTable.filter(_.itunesId === itunesId).map(_.feedRssUrl)
  }

  def updateFeedUrByItunesId(itunesId: Long, feedUrl: String) = {
    readRssFeedUrlByItunesIdRaw(itunesId).update(feedUrl)
  }

  val readPodcastIdFromItunesIdRaw = Compiled { (itunesId: Rep[Long]) =>
    feedTable
      .filter(row => row.itunesId === itunesId && row.podcastDotableId.isDefined)
      .map(_.podcastDotableId.get)
  }

  def readPodcastIdFromItunesId(itunesId: Long): DBIOAction[Option[Long], NoStream, Effect.Read] = {
    readPodcastIdFromItunesIdRaw(itunesId).result.headOption
  }

  val readEpisodesByPodcastIdRaw = Compiled { (podcastId: Rep[Long]) =>
    episodeTable.filter(_.podcastDotableId === podcastId)
  }

  def readEpisodesByPodcastId(podcastId: Long)
    : DBIOAction[Seq[Tables.PodcastEpisodeIngestionRow], NoStream, Effect.Read] = {
    readEpisodesByPodcastIdRaw(podcastId).result
  }

  val filterByItunesIdRaw = Compiled { (itunesId: Rep[Long]) =>
    feedTable.filter(_.itunesId === itunesId)
  }

  def readRowByItunesId(itunesId: Long) = {
    filterByItunesIdRaw(itunesId).result.headOption
  }

  private val readNextIngestionRowsRaw = Compiled {
    (limit: ConstColumn[Long], time: ConstColumn[LocalDateTime]) =>
      feedTable
        .sortBy(_.nextIngestionTime.asc)
        .filter(_.nextIngestionTime < time)
        .take(limit)
  }

  def readNextIngestionRows(limit: Long) = {
    readNextIngestionRowsRaw(limit, LocalDateTime.now()).result
  }

  def readAllPodcastDotableIds() = {
    PodcastQueries.allPodcastDotableIds.result
  }

}
