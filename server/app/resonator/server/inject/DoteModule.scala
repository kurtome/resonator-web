package resonator.server.inject

import com.google.inject.{AbstractModule, Provides}
import play.api.db.slick.DatabaseConfigProvider
import javax.inject.Singleton

import resonator.server.util.LoggingConfig
import slick.basic.BasicBackend

class DoteModule extends AbstractModule {

  def configure() = {
    LoggingConfig.run()
  }

  @Provides
  @Singleton
  def db(dbConfigProvider: DatabaseConfigProvider): BasicBackend#DatabaseDef = {
    dbConfigProvider.get.db
  }

}
