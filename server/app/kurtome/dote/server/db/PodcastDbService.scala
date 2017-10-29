package kurtome.dote.server.db

import javax.inject._

import dote.proto.api.dotable.Dotable
import kurtome.dote.server.controllers.podcast.RssFetchedPodcast
import kurtome.dote.slick.db.DotableKinds
import slick.basic.BasicBackend
import kurtome.dote.slick.db.DotePostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PodcastDbService @Inject()(db: BasicBackend#Database, dotableDbIo: DotableDbIo)(
    implicit ec: ExecutionContext) {

  def ingestPodcast(podcast: RssFetchedPodcast): Future[Long] = {
    val insertPodcastRowOp = (for {
      podcastId <- dotableDbIo.insertPodcastAndGetId(podcast)
      _ <- dotableDbIo.insertEpisodeBatch(podcastId, podcast.episodes)
    } yield podcastId).transactionally

    db.run(insertPodcastRowOp)
  }

  def readPodcastWithEpisodes(id: Long): Future[Option[Dotable]] = {
    val op = for {
      podcast <- dotableDbIo.readHeadById(DotableKinds.Podcast, id)
      episodes <- dotableDbIo.readByParentId(DotableKinds.PodcastEpisode, id)
    } yield podcast.map(_.update(_.relatives.children := episodes))
    db.run(op)
  }

}
