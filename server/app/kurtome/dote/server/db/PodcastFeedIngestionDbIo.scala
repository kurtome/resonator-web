package kurtome.dote.server.db

import java.time.LocalDateTime

import javax.inject._
import kurtome.dote.server.ingestion.RssFetchedEpisode
import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.gen.Tables

import scala.concurrent.ExecutionContext

@Singleton
class PodcastFeedIngestionDbIo @Inject()(implicit ec: ExecutionContext) {

  private val feedTable = Tables.PodcastFeedIngestion
  private val episodeTable = Tables.PodcastEpisodeIngestion

  object EpisodeQueries {
    val selectDataHashByDotableId = Compiled { (episodeDotableId: Rep[Long]) =>
      for {
        row <- episodeTable.filter(_.episodeDotableId === episodeDotableId)
      } yield row.lastDataHash
    }
  }

  def insertEpisodeRecords(podcastId: Long,
                           episodeIdAndFetchedData: Seq[(Long, RssFetchedEpisode)]) = {
    val rows = episodeIdAndFetchedData map {
      case (episodeId, fetchedEpisode) =>
        Tables.PodcastEpisodeIngestionRow(
          id = 0,
          podcastDotableId = podcastId,
          guid = fetchedEpisode.details.rssGuid,
          episodeDotableId = episodeId,
          lastDataHash = Some(fetchedEpisode.dataHash)
        )
    }
    episodeTable ++= rows
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
                                    nextIngestionTime: LocalDateTime) = {
    val q = for {
      row <- feedTable.filter(_.itunesId === itunesId)
    } yield
      (row.podcastDotableId,
       row.feedRssUrl,
       row.lastFeedEtag,
       row.lastDataHash,
       row.nextIngestionTime)
    q.update((Some(podcastDotableId), feedUrl, feedEtag, Some(dataHash), nextIngestionTime))
  }

  def updateNextIngestionTimeByItunesId(itunesId: Long, nextIngestionTime: LocalDateTime) = {
    val q = for {
      row <- feedTable.filter(_.itunesId === itunesId)
    } yield row.nextIngestionTime
    q.update(nextIngestionTime)
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

}
