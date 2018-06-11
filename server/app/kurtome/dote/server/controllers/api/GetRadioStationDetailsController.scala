package kurtome.dote.server.controllers.api

import javax.inject._
import kurtome.dote.proto.api.action.get_radio_station_details._
import kurtome.dote.proto.api.radio.RadioStationDetails
import kurtome.dote.server.controllers.radio.RadioStationFetcher
import kurtome.dote.server.db.mappers.RadioStationMapper
import kurtome.dote.server.services.AuthTokenService
import kurtome.dote.server.services.DotableService
import kurtome.dote.server.services.RadioService
import kurtome.dote.shared.mapper.StatusMapper
import kurtome.dote.shared.util.result.ErrorStatus
import kurtome.dote.shared.util.result.StatusCodes
import kurtome.dote.shared.util.result.SuccessStatus
import play.api.mvc._
import wvlet.log.LogSupport

import scala.concurrent._

@Singleton
class GetRadioStationDetailsController @Inject()(
    cc: ControllerComponents,
    radioStationFetcher: RadioStationFetcher,
    radioService: RadioService,
    dotableService: DotableService,
    authTokenService: AuthTokenService)(implicit ec: ExecutionContext)
    extends ProtobufController[GetRadioStationDetailsRequest, GetRadioStationDetailsResponse](cc)
    with LogSupport {

  override def parseRequest(bytes: Array[Byte]) =
    GetRadioStationDetailsRequest.parseFrom(bytes)

  override def action(request: Request[ActionRequest]) = {
    for {
      detailsOpt <- radioStationFetcher.fetch(request.body.callSign)
    } yield
      GetRadioStationDetailsResponse(stationDetails = detailsOpt)
        .withResponseStatus(StatusMapper.toProto(detailsOpt match {
          case Some(_) => SuccessStatus
          case None => ErrorStatus(StatusCodes.NotFound)
        }))
  }

}
