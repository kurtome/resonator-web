package resonator.shared.constants

import resonator.proto.api.dote.Dote.EmoteKind

object Emojis {
  val cryingFace = "\uD83D\uDE22"
  val heartEyes = "\uD83D\uDE0D"
  val unamusedFace = "\uD83D\uDE12"
  val slightlySmilingFace = "\uD83D\uDE42"
  val grinningFace = "\uD83D\uDE04"
  val slightlyFrowningFace = "\uD83D\uDE41"
  val disappointedFace = "\uD83D\uDE1E"
  val loudlyCryingFace = "\uD83D\uDE2D"
  val grinningSquintingFace = "\uD83D\uDE06"
  val faceWithTearsOfJoy = "\uD83D\uDE02"
  val rollingOnTheFloorLaughing = "\uD83E\uDD23"
  val angryFace = "\uD83D\uDE20"
  val poutingFace = "\uD83D\uDE21"
  val faceWithSymbolsOnMouth = "\uD83E\uDD2C"
  val heart = "❤️"


  val smileEmojis = Seq(heart, grinningFace, heartEyes)

  val laughEmojis = Seq(grinningSquintingFace, faceWithTearsOfJoy, rollingOnTheFloorLaughing)

  val cryEmojis = Seq(disappointedFace, cryingFace, loudlyCryingFace)

  val scowlEmojis = Seq(angryFace, poutingFace, faceWithSymbolsOnMouth)

  def pickEmoji(emoteKind: EmoteKind, default: String = ""): String = {
    import EmoteKind._
    emoteKind match {
      case HEART => heart
      case LAUGH => grinningSquintingFace
      case CRY => cryingFace
      case SCOWL => angryFace
      case _ => default
    }
  }
}
