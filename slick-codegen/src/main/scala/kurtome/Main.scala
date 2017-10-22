package kurtome.slick.codegen

object Main {

  def main(args: Array[String]): Unit = {
    val profile = "slick.jdbc.PostgresProfile"
    val jdbcDriver = "org.postgresql.Driver"
    val jdbcUrl = System.getenv("POSTGRESSQL_DATABASE_URL")
    val scalaPackage = "kurtome.dote.slick.db.gen"

    // TODO - generate code into a "target" directory instead of a "src" directory
    val outputDir = "slick-codegen/src/main/scala"

    println("Generating slick DB table code.")

    slick.codegen.SourceCodeGenerator.main(
      Array(profile, jdbcDriver, jdbcUrl, outputDir, scalaPackage)
    )
  }
}
