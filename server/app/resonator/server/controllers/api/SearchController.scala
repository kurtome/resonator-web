package resonator.server.controllers.api

import javax.inject._
import resonator.proto.api.action.search._
import resonator.server.search.SearchClient
import resonator.server.services.DotableService
import resonator.shared.mapper.StatusMapper
import resonator.shared.util.result.SuccessStatus
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
