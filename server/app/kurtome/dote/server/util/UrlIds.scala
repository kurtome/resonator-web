package kurtome.dote.server.util

import kurtome.dote.server.util.UrlIds.IdKinds.IdKind
import org.pico.hashids.Hashids

/**
  * Encode/decodes IDs from the database to the public URL version, uses http://hashids.org/scala/
  */
object UrlIds {

  private val salt: String = ">CM57dVeU?Tbajk8pAS/Bi3AWudoQ`Zwoj=S=Ho=xVwfLapHyDa;Zoyd4Uj6[[us"

  /**
    * Used to differentiate sequential IDs, so that after encoding:
    *   1 in sequence KindA != 1 in sequence KindB
    */
  object IdKinds extends Enumeration {
    type IdKind = Value
    // Numeric values must never change, as it is used in the encoding.
    val Dotable = Value(1)
    val Person = Value(2)
  }

  private val encoder = Hashids.reference(
    salt = salt,
    minHashLength = 6
  )

  def encode(kind: IdKind, id: Long): String = {
    encoder.encode(kind.id, id)
  }

  def decode(kind: IdKind, urlId: String): Long = {
    encoder.decode(urlId) match {
      // Only match for a list of length one since this isn't meant for arrays of IDs
      case kindId :: id :: Nil => id
      case _ => throw new IllegalStateException("Malformed ID.")
    }
  }

  def decodePerson(urlId: String): Long = decode(IdKinds.Person, urlId)
  def decodeDotable(urlId: String): Long = decode(IdKinds.Dotable, urlId)

}
