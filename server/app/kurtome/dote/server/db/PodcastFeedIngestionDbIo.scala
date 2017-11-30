package kurtome.dote.server.db

import java.time.LocalDateTime
import javax.inject._

import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.gen.Tables

import scala.concurrent.ExecutionContext

@Singleton
class PodcastFeedIngestionDbIo @Inject()(implicit ec: ExecutionContext) {

  private val table = Tables.PodcastFeedIngestion
  private val episodeTable = Tables.PodcastEpisodeIngestion

  def insertEpisodeRecords(podcastId: Long, episodeIdAndGuids: Seq[(Long, String)]) = {
    val rows = episodeIdAndGuids map {
      case (episodeId, guid) =>
        Tables.PodcastEpisodeIngestionRow(
          id = 0,
          podcastDotableId = podcastId,
          guid = guid,
          episodeDotableId = episodeId
        )
    }
    episodeTable ++= rows
  }

  def insertIfNew(itunesId: Long, feedUrl: String) = {
    sqlu"""INSERT INTO podcast_feed_ingestion (itunes_id, feed_rss_url)
         SELECT ${itunesId}, ${feedUrl}
         WHERE NOT EXISTS (
         SELECT 1 FROM dote.public.podcast_feed_ingestion pfi WHERE pfi.itunes_id = ${itunesId})"""
  }

  def updatePodcastRecordByItunesId(podcastDotableId: Long,
                                    itunesId: Long,
                                    feedUrl: String,
                                    feedEtag: Option[String],
                                    nextIngestionTime: LocalDateTime) = {
    val q = for {
      row <- table.filter(_.itunesId === itunesId)
    } yield (row.podcastDotableId, row.feedRssUrl, row.lastFeedEtag, row.nextIngestionTime)
    q.update((Some(podcastDotableId), feedUrl, feedEtag, nextIngestionTime))
  }

  def updateFeedUrByItunesId(itunesId: Long, feedUrl: String) = {
    table.filter(_.itunesId === itunesId).map(_.feedRssUrl).update(feedUrl)
  }

  def readPodcastIdFromItunesId(itunesId: Long): DBIOAction[Option[Long], NoStream, Effect.Read] = {
    table
      .filter(row => row.itunesId === itunesId && row.podcastDotableId.isDefined)
      .map(_.podcastDotableId.get)
      .result
      .headOption
  }

  def readEpisodesByPodcastId(podcastId: Long)
    : DBIOAction[Seq[Tables.PodcastEpisodeIngestionRow], NoStream, Effect.Read] = {
    episodeTable.filter(_.podcastDotableId === podcastId).result
  }

  def readRowByItunesId(itunesId: Long) = {
    table.filter(_.itunesId === itunesId).result.headOption
  }

}
