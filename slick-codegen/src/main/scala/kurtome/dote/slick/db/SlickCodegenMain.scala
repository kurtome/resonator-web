package kurtome.dote.slick.db

object SlickCodegenMain {

  def main(args: Array[String]): Unit = {
    //val profile = "slick.jdbc.PostgresProfile"
    val profile = "kurtome.dote.slick.db.DotePostgresProfile"
    val jdbcDriver = "org.postgresql.Driver"
    val jdbcUrl = System.getenv("POSTGRESSQL_DATABASE_URL")
    val scalaPackage = "kurtome.dote.slick.db.gen"

    assert(jdbcUrl != null && jdbcUrl.nonEmpty,
           "jdbc URL required in env variable POSTGRESSQL_DATABASE_URL")

    // TODO - generate code into a "target" directory instead of a "src" directory
    val outputDir = "slick-codegen/src/main/scala"

    println("Generating slick DB table code.")

    slick.codegen.SourceCodeGenerator.run(
      profile,
      jdbcDriver,
      jdbcUrl,
      outputDir,
      scalaPackage,
      password = None,
      user = None,
      ignoreInvalidDefaults = true,
      codeGeneratorClass = Some("kurtome.dote.slick.db.RichSourceCodeGenerator")
    )
  }

}
