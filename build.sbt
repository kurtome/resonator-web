name := """dote-web"""
organization := "kurtome"
version := "1.0-SNAPSHOT"

val scalaV = "2.12.4"
val postgresJdbcDriver
  : ModuleID = "org.postgresql" % "postgresql" % "9.4-1200-jdbc41" exclude ("org.slf4j", "slf4j-simple")

lazy val server = (project in file("server"))
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .disablePlugins(PlayLogback)
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
      "org.picoworks" %% "pico-hashids" % "4.4.141",
      "com.vmunier" %% "scalajs-scripts" % "1.1.1",
      "org.wvlet.airframe" %% "airframe-log" % "0.30",
      "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.3.2",
      "com.typesafe.play" %% "play-slick" % "3.0.1" exclude ("org.slf4j", "slf4j-simple"),
      "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1" exclude ("org.slf4j", "slf4j-simple"),
      postgresJdbcDriver,
      "com.github.tminglei" %% "slick-pg" % "0.15.4",
      "com.github.tminglei" %% "slick-pg_json4s" % "0.15.4",
      "com.sendgrid" % "sendgrid-java" % "4.1.2",
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
      "org.slf4j" % "slf4j-jdk14" % "1.7.25" // this ensures all logs are sent to airframe-log
    ),
    assemblyMergeStrategy in assembly := {
      // configure sbt-assembly to ignore class files included twice in dependency jars
      // https://github.com/sbt/sbt-assembly#merge-strategy
      case PathList("com", "google", "protobuf", xs @ _ *) => MergeStrategy.first
      case PathList("play", "api", "libs", xs @ _ *) => MergeStrategy.first
      case PathList("play", "reference-overrides.conf") => MergeStrategy.first
      case PathList("org", "apache", "commons", "logging", xs @ _ *) => MergeStrategy.first
      case x => {
        // use the default for everything else
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
      }
    },
    assemblyJarName in assembly := "dote-web-server.jar"
  )
  .dependsOn(sharedJvm, slickCodegen)

val slickCodegenBaseDir = file("slick-codegen")
lazy val slickCodegen = (project in slickCodegenBaseDir)
  .settings(
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick-codegen" % "3.2.1",
      "com.typesafe.slick" %% "slick" % "3.2.1",
      "com.github.tminglei" %% "slick-pg" % "0.15.4",
      "com.github.tminglei" %% "slick-pg_json4s" % "0.15.4",
      "org.json4s" %% "json4s-native" % "3.5.3",
      postgresJdbcDriver
    )
  )

val feedScraperBaseDir = file("feed-scraper")
lazy val feedScraper = (project in feedScraperBaseDir)
  .settings(
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.21",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
      "com.github.scopt" %% "scopt" % "3.7.0",
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      postgresJdbcDriver
    )
  )
  .dependsOn(sharedJvm)

val webBaseDir = file("web")
lazy val web = (project in webBaseDir)
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    scalaVersion := scalaV,
    // https://scalacenter.github.io/scalajs-bundler/reference.html#bundling-mode-library-only
    webpackBundlingMode := BundlingMode.LibraryOnly(),
    webpackConfigFile := Some(baseDirectory.value / "webpack.config.js"),
    // This is an application with a main method
    scalaJSUseMainModuleInitializer := true,
    // Scala libraries that are Scalajs compatible
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.2",
      "org.querki" %%% "jquery-facade" % "1.2",
      "org.querki" %%% "querki-jsext" % "0.8",
      "org.wvlet.airframe" %%% "airframe-log" % "0.30",
      "com.github.japgolly.scalajs-react" %%% "core" % "1.1.0",
      "com.github.japgolly.scalajs-react" %%% "extra" % "1.1.0",
      "com.github.japgolly.scalacss" %%% "core" % "0.5.3",
      "com.github.japgolly.scalacss" %%% "ext-react" % "0.5.3"
    ),
    // Pure javascript libraries (from npm)
    npmDependencies in Compile ++= Seq(
      "json-loader" -> "latest",
      "debounce" -> "1.1.0",
      "linkifyjs" -> "2.1.5",
      "jquery" -> "3.2.1",
      "react" -> "15.6.1",
      "react-autosuggest" -> "9.3.2",
      "react-dom" -> "15.6.1",
      "lscache" -> "1.1.0",
      "lz-string" -> "1.4.4",
      "howler" -> "2.0.5",
      "sanitize-html" -> "1.15.0",
      "siriwavejs" -> "2.0.2",
      "material-ui" -> "next", // Using "next" to use the 1.0 release while its in beta
      "material-ui-icons" -> "1.0.0-beta.15",
      "is-touch-device" -> "1.0.1",
      "react-lazyload" -> "2.3.0"
    )
  )
  .dependsOn(sharedJs)

val sharedBaseDir = file("shared")
lazy val shared = (crossProject.crossType(CrossType.Pure) in sharedBaseDir)
  .settings(
    scalaVersion := scalaV
  )
  .dependsOn(proto)
  .jvmSettings()
  .jsSettings()

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

val protoBaseDir = file("proto")
lazy val proto = (crossProject.crossType(CrossType.Pure) in protoBaseDir)
  .settings(
    scalaVersion := scalaV,
    // Maark the proto directory as a resources root so it's picked up by the IDE as well
    unmanagedResourceDirectories in Compile += protoBaseDir / "res",
    // Define the location for proto source files.
    PB.protoSources in Compile += protoBaseDir / "res",
    PB.protoSources in Compile += target.value / "protobuf_external",
    // Configure location for generated proto source code.
    PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value),
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %%% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion
    )
  )
  .jvmSettings()
  .jsSettings()

lazy val protoJvm = proto.jvm
lazy val protoJs = proto.js

// Auto format with scalafmt on compile
scalafmtOnCompile in ThisBuild := true

lazy val assembleJarAndDeployToHeroku = taskKey[Unit]("Execute frontend scripts")

assembleJarAndDeployToHeroku := {
  val deploy =
    ("heroku deploy:jar --app dote-web /Users/kmelby/github/kurtome/dote-web/server/target/scala-2.12/dote-web-server.jar" !)
  ()
}

assembleJarAndDeployToHeroku := (assembleJarAndDeployToHeroku dependsOn (assembly in server)).value
