package resonator.server.controllers.api

import javax.inject._
import resonator.proto.api.action.get_radio_station_details._
import resonator.proto.api.radio.RadioStationDetails
import resonator.server.controllers.radio.RadioStationFetcher
import resonator.server.db.mappers.RadioStationMapper
import resonator.server.services.AuthTokenService
import resonator.server.services.DotableService
import resonator.server.services.RadioService
import resonator.shared.mapper.StatusMapper
import resonator.shared.util.result.ErrorStatus
import resonator.shared.util.result.StatusCodes
import resonator.shared.util.result.SuccessStatus
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
