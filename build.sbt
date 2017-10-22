name := """dote-web"""
organization := "kurtome"
version := "1.0-SNAPSHOT"

val scalaV = "2.12.3"
val postgresJdbcDriver
  : ModuleID = "org.postgresql" % "postgresql" % "9.4-1200-jdbc41" exclude ("org.slf4j", "slf4j-simple")

lazy val server = (project in file("server"))
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .settings(
    scalaVersion := scalaV,
    // Include JS output from web project
    scalaJSProjects := Seq(web),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    // triggers scalaJSPipeline when using compile or continuous compilation
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    // main class for fat jar
    mainClass in assembly := Some("play.core.server.ProdServerStart"),
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),
    libraryDependencies ++= Seq(
      guice,
      ws,
      "com.vmunier" %% "scalajs-scripts" % "1.1.1",
      "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.3.2",
      "com.typesafe.play" %% "play-slick" % "3.0.1" exclude ("org.slf4j", "slf4j-simple"),
      "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1" exclude ("org.slf4j", "slf4j-simple"),
      postgresJdbcDriver,
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
    ),
    assemblyMergeStrategy in assembly := {
      // configure sbt-assembly to ignore class files included twice in dependency jars
      // https://github.com/sbt/sbt-assembly#merge-strategy
      case PathList("com", "google", "protobuf", xs @ _ *) => MergeStrategy.first
      case PathList("play", "api", "libs", xs @ _ *) => MergeStrategy.first
      case PathList("play", "reference-overrides.conf") => MergeStrategy.first
      case x => {
        // use the default for everything else
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
      }
    },
    assemblyJarName in assembly := "dote-web-server.jar"

    // Adds additional packages into Twirl
    //TwirlKeys.templateImports += "kurtome.controllers._"

    // Adds additional packages into conf/routes
    // play.sbt.routes.RoutesKeys.routesImport += "kurtome.binders._"
  )
  .dependsOn(sharedJvm, slickCodegen)

val slickCodegenBaseDir = file("slick-codegen")
lazy val slickCodegen = (project in slickCodegenBaseDir)
  .settings(
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick-codegen" % "3.2.1",
      "com.typesafe.slick" %% "slick" % "3.2.1",
      postgresJdbcDriver
    )
  )

val webBaseDir = file("web")
lazy val web = (project in webBaseDir)
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    scalaVersion := scalaV,
    // This is an application with a main method
    scalaJSUseMainModuleInitializer := true,
    // Scala libraries that are Scalajs compatible
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.2",
      "org.querki" %%% "jquery-facade" % "1.2",
      "org.querki" %%% "querki-jsext" % "0.8",
      "com.github.japgolly.scalajs-react" %%% "core" % "1.1.0",
      "com.github.japgolly.scalajs-react" %%% "extra" % "1.1.0",
      "com.github.japgolly.scalacss" %%% "core" % "0.5.3",
      "com.github.japgolly.scalacss" %%% "ext-react" % "0.5.3"
    ),
    // Pure javascript libraries
    npmDependencies in Compile ++= Seq(
      "jquery" -> "3.2.1",
      "react" -> "15.6.1",
      "react-dom" -> "15.6.1",
      "material-ui" -> "next", // Using "next" to use the 1.0 release while its in beta
      "material-ui-icons" -> "1.0.0-beta.15"
    )
  )
  .dependsOn(sharedJs)

val sharedBaseDir = file("shared")
lazy val shared = (crossProject.crossType(CrossType.Pure) in sharedBaseDir)
  .settings(
    scalaVersion := scalaV,
    // Define the location for proto source files.
    PB.protoSources in Compile += sharedBaseDir / "proto",
    PB.protoSources in Compile += target.value / "protobuf_external",
    // Configure location for generated proto source code.
    PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value),
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %%% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion
    )
  )
  .jvmSettings()
  .jsSettings()

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command
  .process("project server", _: State)) compose (onLoad in Global).value

// Auto format with scalafmt on compile
scalafmtOnCompile in ThisBuild := true

lazy val assembleJarAndDeployToHeroku = taskKey[Unit]("Execute frontend scripts")

assembleJarAndDeployToHeroku := {
  val deploy =
    ("heroku deploy:jar --app dote-web /Users/kmelby/github/kurtome/dote-web/server/target/scala-2.12/dote-web-server.jar" !)
  ()
}

assembleJarAndDeployToHeroku := (assembleJarAndDeployToHeroku dependsOn (assembly in server)).value
