package kurtome.dote.server.util

import javax.inject._

import play.api.Configuration
import wvlet.log.LogFormatter.SourceCodeLogFormatter
import wvlet.log._

object LoggingConfig extends LogSupport {

  private val rawEnv = System.getProperty("kurtome.dote.env")
  val env = if (rawEnv == null || rawEnv.isEmpty) "prod" else rawEnv

  def run() {
    Logger.setDefaultFormatter(SourceCodeLogFormatter)
    Logger.setDefaultLogLevel(LogLevel.WARN)

    // there is a bug in wvlet.log.Logger where this only reads from "log.properties" at the moment
    val logProperties = s"$env-log.properties"
    Logger.scanLogLevels(Seq(logProperties))

    info(s"Initialized logging with config file: $logProperties")
  }
}
