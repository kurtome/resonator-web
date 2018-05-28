addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.3")
addSbtPlugin("org.scala-js"     % "sbt-scalajs" % "0.6.23")
addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.6")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.1")

// This plugin enables depending on pure Javascript libraries via NPM
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.8.0")
addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.8.0")

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.12")

// For deploying fat jar to heroku
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")

// For debugging transitive dependencies
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")