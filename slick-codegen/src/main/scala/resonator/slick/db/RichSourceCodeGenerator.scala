package resonator.slick.db

import slick.codegen.{OutputHelpers, SourceCodeGenerator}
import slick.{model => m}

/**
  * Copied from [[slick.codegen.SourceCodeGenerator]] to add support for more types
  */
class RichSourceCodeGenerator(model: m.Model)
    extends SourceCodeGenerator(model)
    with OutputHelpers {

  override def packageCode(profile: String,
                           pkg: String,
                           container: String,
                           parentType: Option[String]): String = {
    val originalCode = super.packageCode(profile, pkg, container, parentType)
    // Hack in the profile with all the new type supported, this makes the implicit
    // conversions work to/from the new types
    originalCode
      .replace("val profile: slick.jdbc.JdbcProfile",
               "val profile: resonator.slick.db.DotePostgresProfile")
      .replace("_tableTag: Tag", "_tableTag: slick.lifted.Tag")
      // TODO: make the postgres profile use produce a
      // Array[Byte] type instead of Blob for a 'bytea'
      // column type
      .replace("java.sql.Blob", "Array[Byte]")
  }

  override def Table = new TableDef(_)

  class TableDef(model: m.Table) extends super.TableDef(filterDbColumns(model)) {}

  // Don't generate the db managed columns, they are set via db triggers and best to just ignore
  // in the app code.
  private def filterDbColumns(model: m.Table) = {
    model.copy(
      columns = model.columns.filter(c => {
        c.name != "db_created_time"
      })
    )
  }

}
