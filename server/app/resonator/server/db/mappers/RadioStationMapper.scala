package resonator.server.db.mappers

import resonator.proto.api.radio.RadioStation
import resonator.slick.db.gen.Tables

object RadioStationMapper {

  def toProto(station: Tables.RadioStationRow): RadioStation = {
    RadioStation(
      callSign = station.callSign,
      frequency = station.frequency.toFloat,
      frequencyKind = station.frequencyKind match {
        case "AM" => RadioStation.FrequencyKind.AM
        case "FM" => RadioStation.FrequencyKind.AM
        case _ => RadioStation.FrequencyKind.UNKNOWN_KIND
      }
    )
  }

}
