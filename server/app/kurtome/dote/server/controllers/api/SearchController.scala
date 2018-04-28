package kurtome.dote.server.controllers.api

import javax.inject._
import kurtome.dote.proto.api.action.search._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.server.db.mappers.DotableMapper
import kurtome.dote.server.search.SearchClient
import kurtome.dote.server.services.DotableService
import kurtome.dote.shared.constants.DotableKinds
import kurtome.dote.shared.constants.DotableKinds.DotableKind
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.SuccessStatus
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class SearchController @Inject()(cc: ControllerComponents,
                                 podcastDbService: DotableService,
                                 searchClient: SearchClient)(implicit ec: ExecutionContext)
    extends ProtobufController[SearchRequest, SearchResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    SearchRequest.parseFrom(bytes)

  override def action(request: Request[SearchRequest]) = {

    for {
      combinedResults <- searchClient.searchAll(request.body.query)
      podcastResults <- searchClient.searchPodcast(request.body.query)
      episodeResults <- searchClient.searchEpisode(request.body.query)
    } yield
      SearchResponse(
        Some(StatusMapper.toProto(SuccessStatus)),
        Seq(
          SearchResponse.ResultsByKind(kind = Dotable.Kind.PODCAST, dotables = podcastResults),
          SearchResponse.ResultsByKind(kind = Dotable.Kind.PODCAST_EPISODE,
                                       dotables = episodeResults)
        ),
        request.body.query
      ).withCombinedResults(combinedResults)
  }

  private def searchByKind(request: SearchRequest,
                           kind: DotableKind): Future[SearchResponse.ResultsByKind] = {
    podcastDbService.search(request.query, kind, request.maxResults) map { list =>
      SearchResponse.ResultsByKind(kind = DotableMapper.mapKind(kind), dotables = list)
    }
  }

}
