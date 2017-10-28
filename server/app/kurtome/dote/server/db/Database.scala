package kurtome.dote.server.db

import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneOffset}
import javax.inject._

import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion}
import com.trueaccord.scalapb.json.JsonFormat
import dote.proto.api.dotable.Dotable
import kurtome.dote.slick.db.DotableKinds
import kurtome.dote.slick.db.gen.Tables
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scalapb.descriptors.ScalaType.Message

@Singleton
class Database @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val db = dbConfigProvider.get.db

  def ingestPodcast(podcast: Dotable): Future[Unit] = {
    val common = podcast.getCommon
    val op = (for {
      id <- (Tables.Dotable returning Tables.Dotable.map(_.id)) +=
        Tables.DotableRow(
          id = 0,
          kind = DotableKinds.Podcast,
          Some(common.title),
          Some(common.description),
          publishedTime = LocalDateTime.ofEpochSecond(common.publishedEpochSec, 0, ZoneOffset.UTC),
          editedTime = LocalDateTime.ofEpochSecond(common.updatedEpochSec, 0, ZoneOffset.UTC),
          parentId = None,
          details = JsonFormat.toJson(podcast.getDetails),
          // These should be set by the DB
          dbCreatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC),
          dbUpdatedTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
        )
    } yield ()).transactionally
    db.run(op)
  }

}
