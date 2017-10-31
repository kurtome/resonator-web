package kurtome.dote.server.db

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

  def insert(podcastDotableId: Long, itunesId: Long, feedUrl: String) = {
    assert(podcastDotableId > 0)
    assert(itunesId > 0)
    assert(!feedUrl.isEmpty)
    val row = Tables.PodcastFeedIngestionRow(
      id = 0,
      feedRssUrl = feedUrl,
      podcastDotableId = podcastDotableId,
      itunesId = itunesId
    )
    table += row
  }

  def updateFeedUrByItunesId(itunesId: Long, feedUrl: String) = {
    table.filter(_.itunesId === itunesId).map(_.feedRssUrl).update(feedUrl)
  }

  def readPodcastIdFromItunesId(itunesId: Long): DBIOAction[Option[Long], NoStream, Effect.Read] = {
    table.filter(_.itunesId === itunesId).map(_.podcastDotableId).result.headOption
  }

  def readEpisodesByPodcastId(podcastId: Long)
    : DBIOAction[Seq[Tables.PodcastEpisodeIngestionRow], NoStream, Effect.Read] = {
    episodeTable.filter(_.podcastDotableId === podcastId).result
  }
}
