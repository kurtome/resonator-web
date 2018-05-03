package kurtome.dote.server.controllers.api

import javax.inject._
import kurtome.dote.proto.api.action.search._
import kurtome.dote.server.search.SearchClient
import kurtome.dote.server.services.DotableService
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.SuccessStatus
import play.api.mvc._

import scala.concurrent.ExecutionContext

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
    } yield
      SearchResponse(
        Some(StatusMapper.toProto(SuccessStatus)),
        request.body.query
      ).withCombinedResults(combinedResults)
  }

}
