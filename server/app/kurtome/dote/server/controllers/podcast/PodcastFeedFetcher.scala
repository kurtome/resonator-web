package kurtome.dote.server.controllers.podcast

import com.google.inject._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PodcastFeedFetcher @Inject()(ws: WSClient, parser: PodcastFeedParser) { self =>

  def fetch(url: String): Future[Seq[RssFetchedPodcast]] = {
    ws.url(url).get() map { response =>
      // Remove any spurious leading characters, which will break the parsing
      val xmlString = response.body.substring(response.body.indexOf('<'))
      parser.parsePodcastRss(url, xmlString)
    }
  }

}
