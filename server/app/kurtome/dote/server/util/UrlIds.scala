package kurtome.dote.server.util

import org.pico.hashids.Hashids

/**
  * Encode/decodes IDs from the database to the public URL version, uses http://hashids.org/scala/
  */
object UrlIds {

  private val salt: String = ">CM57dVeU?Tbajk8pAS/Bi3AWudoQ`Zwoj=S=Ho=xVwfLapHyDa;Zoyd4Uj6[[us"

  private val encoder = Hashids.reference(
    salt = salt,
    minHashLength = 6
  )

  def encode(id: Long): String = {
    encoder.encode(id)
  }

  def decode(urlId: String): Long = {
    encoder.decode(urlId) match {
      // Only match for a list of length one since this isn't meant for arrays of IDs
      case id :: Nil => id
    }
  }

}
