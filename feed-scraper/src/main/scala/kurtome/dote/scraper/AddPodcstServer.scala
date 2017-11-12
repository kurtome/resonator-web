package kurtome.dote.scraper

import dote.proto.api.action.add_podcast.{AddPodcastRequest, AddPodcastResponse}
import kurtome.dote.scraper.ScraperMain.ScraperConfig

import scalaj.http.{Http, HttpOptions}

object AddPodcstServer {

  private val localApiHost = "http://localhost:9000/api/v1/"
  private val prodApiHost = "http://www.podfeels.com/api/v1/"

  def addPodcast(request: AddPodcastRequest)(implicit config: ScraperConfig): AddPodcastResponse = {
    val route = if (config.prod) {
      prodApiHost
    } else {
      localApiHost
    }

    val result = Http(localApiHost + "add-podcast")
      .postData(request.toByteArray)
      .header("Content-Type", "application/x-protobuf")
      .option(HttpOptions.readTimeout(50000))
      .asBytes
    AddPodcastResponse.parseFrom(result.body)
  }

}
