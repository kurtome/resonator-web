package kurtome.dote.server.controllers.podcast

import javax.inject._

import com.trueaccord.scalapb.json.JsonFormat
import dote.proto.external.itunes_entity.ItunesEntity
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItunesEntityFetcher @Inject()(ws: WSClient)(implicit ec: ExecutionContext) {

  def fetch(itunesId: Long, kind: String): Future[ItunesEntity] = {
    val url = s"https://itunes.apple.com/lookup?id=${itunesId}&entity=${kind}"
    ws.url(url).get() map { response =>
      JsonFormat.fromJsonString[ItunesEntity](response.body.trim())
    }
  }

}
