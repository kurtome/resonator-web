package kurtome.dote.server.controllers.api

import javax.inject._
import kurtome.dote.proto.api.action.get_dotable._
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.dotable_list.DotableList
import kurtome.dote.proto.api.feed.Feed
import kurtome.dote.proto.api.feed.FeedDotableList
import kurtome.dote.proto.api.feed.FeedId
import kurtome.dote.proto.api.feed.FeedItem
import kurtome.dote.server.search.SearchClient
import kurtome.dote.server.services.AuthTokenService
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.util.UrlIds
import kurtome.dote.server.util.UrlIds.IdKinds
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.ErrorStatus
import kurtome.dote.shared.util.result.StatusCodes
import kurtome.dote.shared.util.result.SuccessStatus
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class GetDotableDetailsController @Inject()(
    cc: ControllerComponents,
    authTokenService: AuthTokenService,
    dotableService: DotableService,
    searchClient: SearchClient)(implicit ec: ExecutionContext)
    extends ProtobufController[GetDotableDetailsRequest, GetDotableDetailsResponse](cc) {

  override def parseRequest(bytes: Array[Byte]) =
    GetDotableDetailsRequest.parseFrom(bytes)

  override def action(request: Request[GetDotableDetailsRequest]) =
    authTokenService.simplifiedRead(request) flatMap { loggedInPerson =>
      val dotableId = UrlIds.decode(IdKinds.Dotable, request.body.id)
      for {
        dotableOpt <- dotableService.readDotableDetails(dotableId, loggedInPerson.map(_.id))
        moreLike <- dotableOpt match {
          case Some(dotable) => searchClient.moreLike(dotable)
          case _ => Future(Nil)
        }
      } yield
        dotableOpt match {
          case Some(dotable) => {
            val moreLikeTitle =
              s"Similar ${if (dotable.kind == Dotable.Kind.PODCAST) "Podcasts" else "Epsidoes"}"
            val feed = Feed(
              items = moreLike match {
                case Nil => Nil
                case _ =>
                  Seq(
                    FeedItem()
                      .withDotableList(FeedDotableList().withList(
                        DotableList(dotables = moreLike, title = moreLikeTitle)))
                      .withId(FeedId().withDotableList(FeedId.DotableListId())))
              }
            )
            GetDotableDetailsResponse(Some(StatusMapper.toProto(SuccessStatus)), Some(dotable))
              .withFeed(feed)

          }
          case None =>
            GetDotableDetailsResponse(
              Some(StatusMapper.toProto(ErrorStatus(StatusCodes.NotFound))),
              None)
        }
    }

}
