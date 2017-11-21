package kurtome.dote.server.db

import javax.inject._

import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.TagKinds.TagKind
import kurtome.dote.slick.db.gen.Tables
import kurtome.dote.slick.db.gen.Tables.{DotableTagRow, TagRow}

import scala.concurrent.ExecutionContext

@Singleton
class DotableTagDbIo @Inject()(implicit ec: ExecutionContext) {

  private val tagTable = Tables.Tag
  private val table = Tables.DotableTag

  private val readByDotableIdRaw = Compiled { (dotableId: Rep[Long]) =>
    table.filter(_.dotableId === dotableId)
  }

  private val readTagByKeyRaw = Compiled { (key: Rep[String]) =>
    tagTable.filter(_.key === key)
  }

  private val readRowRaw = Compiled { (tagId: Rep[Long], dotableId: Rep[Long]) =>
    table.filter(row => row.tagId === tagId && row.dotableId === dotableId)
  }

  private val dotableTagExistsRaw = Compiled { (tagId: Rep[Long], dotableId: Rep[Long]) =>
    table.filter(row => row.tagId === tagId && row.dotableId === dotableId).exists
  }

  private val readDotableIdsByTabKey = Compiled { (key: Rep[String], limit: ConstColumn[Long]) =>
    (for {
      (t, td) <- tagTable.filter(_.key === key) join table on (_.id === _.tagId)
    } yield (td.dotableId)).take(limit)
  }

  private val tagKeyExistsRaw = Compiled {
    (kind: ConstColumn[TagKind], key: ConstColumn[String]) =>
      tagTable.filter(row => row.kind === kind && row.key === key).exists
  }

  def readTagIdByKeyRaw(key: String) = {
    readTagByKeyRaw(key).result.headOption.map(_.map(_.id))
  }

  def readByDotableId(dotableId: Long) = {
    readByDotableIdRaw(dotableId).result
  }

  def readDotableIdsByTagKey(key: String, limit: Long) = {
    readDotableIdsByTabKey(key, limit).result
  }

  def insertDotableTag(tagId: Long, dotableId: Long) = {
    table += DotableTagRow(tagId = tagId, dotableId = dotableId)
  }

  def insertTag(kind: TagKind, key: String, name: String) = {
    tagTable += TagRow(id = 0, kind = kind, key = key, name = name)
  }

  def dotableTagExists(tagId: Long, dotableId: Long) = {
    dotableTagExistsRaw(tagId, dotableId).result
  }

  def tagKeyExists(kind: TagKind, key: String) = {
    tagKeyExistsRaw(kind, key).result
  }
}
