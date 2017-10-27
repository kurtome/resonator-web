package kurtome.dote.slick.db

import slick.codegen.{AbstractSourceCodeGenerator, OutputHelpers, SourceCodeGenerator}
import slick.jdbc.JdbcModelBuilder
import slick.jdbc.meta.MTable
import slick.{model => m}

import scala.concurrent.ExecutionContext

/**
  * Copied from {@link slick.codegen.SourceCodeGenerator} to add support for more types
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
    originalCode.replace("val profile: slick.jdbc.JdbcProfile",
                         "val profile: kurtome.dote.slick.db.DotePostgresProfile")
  }

}
