package kurtome.dote.server.db.mappers

import kurtome.dote.proto.api.radio.RadioStation
import kurtome.dote.slick.db.gen.Tables

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
