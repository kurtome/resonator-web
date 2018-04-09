package kurtome.dote.shared.mapper

import kurtome.dote.proto.api.dote.Dote.EmoteKind
import kurtome.dote.shared.constants

object EmoteKindMapper {

  def toProto(emoteKind: Option[constants.EmoteKinds.EmoteKind]): EmoteKind = {
    import constants.EmoteKinds._
    emoteKind match {
      case Some(Heart) => EmoteKind.HEART
      case Some(Laugh) => EmoteKind.LAUGH
      case Some(Cry)   => EmoteKind.CRY
      case Some(Scowl) => EmoteKind.SCOWL
      case _     => EmoteKind.UNKNOWN_KIND
    }
  }

  def fromProto(emoteKind: EmoteKind): Option[constants.EmoteKinds.EmoteKind] = {
    import constants.EmoteKinds._
    emoteKind match {
      case EmoteKind.HEART => Some(Heart)
      case EmoteKind.LAUGH => Some(Laugh)
      case EmoteKind.CRY   => Some(Cry)
      case EmoteKind.SCOWL => Some(Scowl)
      case _ => None
    }
  }
}
