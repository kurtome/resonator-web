package controllers.podcast

import com.google.inject._
import play.api.Logger
import play.api.libs.ws.WSClient
import scala.xml.XML

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PodcastFeedFetcher @Inject()(ws: WSClient) { self =>
  val logger = Logger(self.getClass)

  case class Podcast(feedUrl: String,
                     title: String,
                     shortDescription: String,
                     episodes: Seq[PodcastEpisode])
  case class PodcastEpisode(title: String, shortDescription: String)

  def fetch(url: String): Future[Podcast] = {
    ws.url(url).get() map { response =>
      //Logger.info(response.body)

      val channels = XML.loadString(response.body) \ "channel"
      val title: String = (channels \ "title").text
      Logger.info(title)
      Podcast(url, title, "", Seq())
    }
  }
}
