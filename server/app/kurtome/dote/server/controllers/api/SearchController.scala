package kurtome.dote.server.controllers.api

import javax.inject._

import dote.proto.api.action.search._
import kurtome.dote.server.db.{DotableDbService, MetadataFlag}
import kurtome.dote.slick.db.DotableKinds
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchController @Inject()(cc: ControllerComponents, podcastDbService: DotableDbService)(
    implicit ec: ExecutionContext)
    extends ProtobufController[SearchRequest, SearchResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    SearchRequest.parseFrom(bytes)

  override def action(request: SearchRequest): Future[SearchResponse] = {
    podcastDbService
      .search(request.query, DotableKinds.Podcast, request.maxResults)
      .map(list => SearchResponse(list))
  }

}
