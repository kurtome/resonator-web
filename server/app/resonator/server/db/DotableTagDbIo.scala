package resonator.server.db

import java.sql.Types
import java.time.LocalDateTime

import javax.inject._
import resonator.shared
import resonator.slick.db.DotePostgresProfile.api._
import resonator.shared.constants.TagKinds.TagKind
import resonator.shared.model.Tag
import resonator.shared.model.TagId
import resonator.slick.db.gen.Tables
import resonator.slick.db.gen.Tables.{DotableTagRow, TagRow}
import slick.jdbc.SetParameter

import scala.concurrent.ExecutionContext

@Singleton
class DotableTagDbIo @Inject()(implicit ec: ExecutionContext) {

  private val tagTable = Tables.Tag
  private val table = Tables.DotableTag

  private val readByParentDotableIdRaw = Compiled { (dotableId: Rep[Long]) =>
    for {
      d <- Tables.Dotable.filter(_.id === dotableId)
      dt <- table if dt.dotableId === d.parentId
      t <- tagTable if t.id === dt.tagId
    } yield t
  }

  private val readByDotableIdRaw = Compiled { (dotableId: Rep[Long]) =>
    for {
      dt <- table.filter(_.dotableId === dotableId)
      t <- tagTable if t.id === dt.tagId
    } yield t
  }

  private val readTagByKeyRaw = Compiled { (kind: Rep[TagKind], key: Rep[String]) =>
    tagTable.filter(row => row.kind === kind && row.key === key)
  }

  private val readRowRaw = Compiled { (tagId: Rep[Long], dotableId: Rep[Long]) =>
    table.filter(row => row.tagId === tagId && row.dotableId === dotableId)
  }

  private val dotableTagExistsRaw = Compiled { (tagId: Rep[Long], dotableId: Rep[Long]) =>
    table.filter(row => row.tagId === tagId && row.dotableId === dotableId).exists
  }

  private val readDotableIdsByTabKey = Compiled {
    (kind: Rep[TagKind], key: Rep[String], limit: ConstColumn[Long]) =>
      (for {
        t <- tagTable.filter(row => row.kind === kind && row.key === key)
        td <- table if td.tagId === t.id
        d <- Tables.Dotable.sortBy(_.contentEditedTime.desc) if d.id === td.dotableId
      } yield td.dotableId).take(limit)
  }

  private val tagKeyExistsRaw = Compiled {
    (kind: ConstColumn[TagKind], key: ConstColumn[String]) =>
      tagTable.filter(row => row.kind === kind && row.key === key).exists
  }

  def readTagDbId(tagId: TagId) = {
    readTagByKeyRaw(tagId.kind, tagId.key).result.headOption.map(_.map(_.id))
  }

  def readByDotableId(dotableId: Long) = {
    readByDotableIdRaw(dotableId).result.map(_.map { tagRow =>
      Tag(
        kind = tagRow.kind,
        key = tagRow.key,
        name = tagRow.name
      )
    })
  }

  def readByParentDotableId(dotableId: Long) = {
    readByParentDotableIdRaw(dotableId).result.map(_.map { tagRow =>
      Tag(
        kind = tagRow.kind,
        key = tagRow.key,
        name = tagRow.name
      )
    })
  }

  implicit val kindSetter = SetParameter[TagKind] {
    case (kind, params) => params.setObject(s"${kind.toString}", Types.OTHER)
  }

  def upsertTag(tag: shared.model.Tag) = {
    sqlu"""INSERT INTO tag (kind, key, name)
         SELECT ${tag.id.kind}, ${tag.id.key}, ${tag.name}
         WHERE NOT EXISTS (
         SELECT 1 FROM tag t1 WHERE t1.kind = ${tag.id.kind} AND t1.key = ${tag.id.key})"""
  }

  def upsertTagBatch(tags: Seq[shared.model.Tag]) = {
    // Upserts all and returns a sum of affected row count
    DBIO.sequence(tags.map(upsertTag)).map(_.sum)
  }

  def upsertDotableTag(dotableId: Long, tagId: TagId) = {
    sqlu"""INSERT INTO dotable_tag (dotable_id, tag_id)
         SELECT ${dotableId}, t.id
         FROM tag t
         WHERE t.kind = ${tagId.kind} AND t.key = ${tagId.key} AND
         NOT EXISTS (
         SELECT 1 FROM dotable_tag dt WHERE dt.dotable_id = ${dotableId} and dt.tag_id = t.id)"""
  }

  def upsertDotableTagBatch(dotableId: Long, tagIds: Seq[TagId]) = {
    // Upserts all and returns a sum of affected row count
    DBIO.sequence(tagIds.map(upsertDotableTag(dotableId, _))).map(_.sum)
  }

  def readDotableIdsByTagKey(tagId: TagId, limit: Long) = {
    readDotableIdsByTabKey(tagId.kind, tagId.key, limit).result
  }

  def readTagById(tagId: TagId) = {
    readTagByKeyRaw(tagId.kind, tagId.key).result.headOption.map(_.map(row => {
      assert(tagId.kind == row.kind)
      assert(tagId.key == row.key)
      Tag(tagId, row.name)
    }))
  }

  def insertDotableTag(tagId: Long, dotableId: Long) = {
    table += DotableTagRow(tagId = tagId, dotableId = dotableId, dbUpdatedTime = LocalDateTime.MIN)
  }

  def insertTag(tag: Tag) = {
    tagTable += TagRow(id = 0,
                       kind = tag.id.kind,
                       key = tag.id.key,
                       name = tag.name,
                       dbUpdatedTime = LocalDateTime.MIN)
  }

  def insertTagBatch(tags: Seq[Tag]) = {
    tagTable ++= tags.map(
      tag =>
        TagRow(id = 0,
               kind = tag.id.kind,
               key = tag.id.key,
               name = tag.name,
               dbUpdatedTime = LocalDateTime.MIN))
  }

  def dotableTagExists(tagId: Long, dotableId: Long) = {
    dotableTagExistsRaw(tagId, dotableId).result
  }

  def tagKeyExists(kind: TagKind, key: String) = {
    tagKeyExistsRaw(kind, key).result
  }

  def readTagsByKeys(kind: TagKind, keys: Seq[String]) = {
    (for {
      t <- tagTable.filter(row => row.kind === kind && row.key.inSet(keys))
    } yield t).result
  }
}
