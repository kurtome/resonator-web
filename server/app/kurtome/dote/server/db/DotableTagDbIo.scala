package kurtome.dote.server.db

import javax.inject._

import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.slick.db.gen.Tables.{DotableTagRow}

import scala.concurrent.ExecutionContext

@Singleton
class DotableTagDbIo @Inject()(implicit ec: ExecutionContext) {

  private val tagTable = Tables.Tag
  private val table = Tables.DotableTag

  private val readByDotableIdRaw = Compiled { (dotableId: Rep[Long]) =>
    table.filter(_.dotableId === dotableId)
  }

  private val readTagByLabelRaw = Compiled { (label: Rep[String]) =>
    tagTable.filter(_.label === label)
  }

  private val readRowRaw = Compiled { (tagId: Rep[Long], dotableId: Rep[Long]) =>
    table.filter(row => row.tagId === tagId && row.dotableId === dotableId)
  }

  private val readDotableIdsByTabLabel = Compiled {
    (label: Rep[String], limit: ConstColumn[Long]) =>
      (for {
        (t, td) <- tagTable.filter(_.label === label) join table on (_.id === _.tagId)
      } yield (td.dotableId)).take(limit)
  }

  def readTagIdByLabelRaw(label: String) = {
    readTagByLabelRaw(label).result.headOption.map(_.map(_.id))
  }

  def readByDotableId(dotableId: Long) = {
    readByDotableIdRaw(dotableId).result
  }

  def readDotableIdsByTagLabel(label: String, limit: Long) = {
    readDotableIdsByTabLabel(label, limit).result
  }

  def insertDotableTag(tagId: Long, dotableId: Long) = {
    table += DotableTagRow(tagId = Some(tagId), dotableId = Some(dotableId))
  }

  def tagExists(tagId: Long, dotableId: Long) = {
    table.filter(row => row.tagId === tagId && row.dotableId === dotableId).exists.result
  }
}
