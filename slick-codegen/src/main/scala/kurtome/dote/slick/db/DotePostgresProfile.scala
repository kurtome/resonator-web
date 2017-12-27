package kurtome.dote.slick.db

import java.time._

import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.enums.PgEnumExtensions
import org.json4s.{JValue, JsonMethods}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcModelBuilder
import slick.jdbc.meta.{MColumn, MTable}
import slick.profile.Capability

import scala.concurrent.ExecutionContext
import scala.reflect.classTag

object DotableKinds extends Enumeration {
  type DotableKind = Value
  val Podcast = Value("podcast")
  val PodcastEpisode = Value("podcast_episode")
}

object TagKinds extends Enumeration {
  type TagKind = Value
  val MetadataFlag = Value("metadata_flag")
  val PodcastCreator = Value("podcast_creator")
  val PodcastGenre = Value("podcast_genre")
  val Keyword = Value("keyword")
}

trait DotePostgresProfile
    extends ExPostgresProfile
    with PgEnumSupport
    with PgEnumExtensions
    with PgDate2Support
    with PgJson4sSupport
    with PgArraySupport
    with PgSearchSupport {

  override def createModelBuilder(tables: Seq[MTable], ignoreInvalidDefaults: Boolean)(
      implicit ec: ExecutionContext): JdbcModelBuilder =
    new DoteModelBuilder(tables, ignoreInvalidDefaults)

  // This generates columns info, it is overridden to support more types during code generation
  class DoteModelBuilder(mTables: Seq[MTable], ignoreInvalidDefaults: Boolean)(
      implicit ec: ExecutionContext)
      extends super.ExModelBuilder(mTables, ignoreInvalidDefaults) {

    override def createColumnBuilder(tableBuilder: TableBuilder, meta: MColumn): ColumnBuilder = {
      new DoteColumnBuilder(tableBuilder, meta)
    }

    class DoteColumnBuilder(tableBuilder: TableBuilder, meta: MColumn)
        extends ColumnBuilder(tableBuilder, meta) {
      override def tpe: String = {
        val qualifiedClassName = meta.typeName match {
          case "dotablekind" => DotableKinds.getClass.getName + "Value"
          case "tagkind" => TagKinds.getClass.getName + "Value"
          case _ => super.tpe
        }
        // Replace $ with . to fix nested classes in generated code because classTag.toString
        // uses $ to represent nested types.
        qualifiedClassName.replace('$', '.')
      }
    }
  }

  def pgjson = "jsonb"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcProfile.capabilities.insertOrUpdate

  override val api = DoteAPI

  val plainAPI = new API with Json4sJsonPlainImplicits

  type DOCType = org.json4s.native.Document
  override val jsonMethods = org.json4s.native.JsonMethods.asInstanceOf[JsonMethods[DOCType]]

  bindPgDateTypesToScala(classTag[LocalDate],
                         classTag[LocalTime],
                         classTag[LocalDateTime],
                         classTag[OffsetTime],
                         classTag[OffsetDateTime],
                         classTag[Duration])

  object DoteAPI
      extends API
      with ArrayImplicits
      with DateTimeImplicits
      with Json4sJsonImplicits
      with JsonImplicits
      with SearchImplicits
      with SearchAssistants
      with ILikeImplicits {

    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)

    implicit val dotableTypeTypeMapper = createEnumJdbcType("DotableKind", DotableKinds, false)
    implicit val dotableTypeListTypeMapper =
      createEnumListJdbcType("DotableKind", DotableKinds, false)
    implicit val dotableTypeColumnExtensionMethodsBuilder =
      createEnumColumnExtensionMethodsBuilder(DotableKinds)
    implicit val dotableTypeOptionColumnExtensionMethodsBuilder =
      createEnumOptionColumnExtensionMethodsBuilder(DotableKinds)

    implicit val tagKindTypeMapper = createEnumJdbcType("TagKind", TagKinds, false)
    implicit val tagKindListTypeMapper =
      createEnumListJdbcType("TagKind", TagKinds, false)
    implicit val tagKindColumnExtensionMethodsBuilder =
      createEnumColumnExtensionMethodsBuilder(TagKinds)
    implicit val tagKindOptionColumnExtensionMethodsBuilder =
      createEnumOptionColumnExtensionMethodsBuilder(TagKinds)

    implicit val json4sJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JValue](
        pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JValue](jsonMethods.parse(_))(s).orNull,
        (v) =>
          utils.SimpleArrayUtils
            .mkString[JValue](j => jsonMethods.compact(jsonMethods.render(j)))(v)
      ).to(_.toList)
  }

}
object DotePostgresProfile extends DotePostgresProfile
