package resonator.scraper

import resonator.proto.api.action.add_podcast._
import resonator.scraper.ScraperMain.ScraperConfig

import scalaj.http.{Http, HttpOptions}

object AddPodcastServer {

  private val localApiHost = "http://localhost:9000/api/v1/"
  private val prodApiHost = "http://dote-web.herokuapp.com/api/v1/"

  def addPodcast(request: AddPodcastRequest)(implicit config: ScraperConfig): AddPodcastResponse = {
    val route = if (config.prod) {
      prodApiHost
    } else {
      localApiHost
    }

    val result = Http(route + "add-podcast")
      .postData(request.toByteArray)
      .header("Content-Type", "application/x-protobuf")
      .option(HttpOptions.followRedirects(true))
      .option(HttpOptions.readTimeout(60000))
      .asBytes

    AddPodcastResponse.parseFrom(result.body)
  }

}
