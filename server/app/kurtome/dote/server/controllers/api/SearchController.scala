package kurtome.dote.server.controllers.api

import javax.inject._

import kurtome.dote.proto.api.action.search._
import kurtome.dote.server.model.MetadataFlag
import kurtome.dote.server.services.DotableService
import kurtome.dote.slick.db.DotableKinds
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchController @Inject()(cc: ControllerComponents, podcastDbService: DotableService)(
    implicit ec: ExecutionContext)
    extends ProtobufController[SearchRequest, SearchResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    SearchRequest.parseFrom(bytes)

  override def action(request: Request[SearchRequest]) = {
    podcastDbService
      .search(request.body.query, DotableKinds.Podcast, request.body.maxResults)
      .map(list => SearchResponse(list))
  }

}
