package kurtome.dote.server.util

import scala.annotation.tailrec
import scala.util.Random

object RandomString {

  private val uppercaseAlphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
  private val fullAlphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

  def uppercaseAlphanumeric(length: Int): String = {
    generate(uppercaseAlphabet, length)
  }

  def fullAlphanumeric(length: Int): String = {
    generate(fullAlphabet, length)
  }

  @tailrec
  private def generate(alphabet: String, length: Int, prefix: Seq[Char] = ""): String = {
    if (length < 1) prefix.mkString
    else {
      val nextChar: Char = alphabet.charAt(Math.abs(Random.nextInt()) % alphabet.length)
      generate(alphabet, length - 1, prefix :+ nextChar)
    }
  }

}
