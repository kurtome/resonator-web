package kurtome.dote.server.util

import org.scalatest.WordSpec

class UrlIdsTest extends WordSpec {

  "encode" when {
    "single digit ID" should {
      "be length 8" in {
        assert(UrlIds.encode(1).size === 6)
      }
      "encode 1 the same each time" in {
        assert(UrlIds.encode(1) === "AaBzaZ")
        assert(UrlIds.encode(1) === "AaBzaZ")
      }
      "encode 2 the same each time" in {
        assert(UrlIds.encode(2) === "NaMNEr")
        assert(UrlIds.encode(2) === "NaMNEr")
      }
      "encode 3 the same each time" in {
        assert(UrlIds.encode(3) === "3aD6XO")
        assert(UrlIds.encode(3) === "3aD6XO")
      }
    }
    "Long ID" should {
      val longId: Long = Int.MaxValue.toLong + 1L
      "be length 8" in {
        assert(UrlIds.encode(longId).size === 7)
      }
      "Long ID the same each time" in {
        assert(UrlIds.encode(longId) === "Z8WBBMd")
        assert(UrlIds.encode(longId) === "Z8WBBMd")
      }
    }
  }

  "decode" when {
    "single digit ID" should {
      "decode 1 from constant" in {
        assert(UrlIds.decode("AaBzaZ") === 1)
      }
    }
    "reflexive with encode" should {
      "514356658" in {
        assert(UrlIds.decode(UrlIds.encode(514356658)) === 514356658)
      }
      "9" in {
        assert(UrlIds.decode(UrlIds.encode(9)) === 9)
      }
      "100" in {
        assert(UrlIds.decode(UrlIds.encode(100)) === 100)
      }
    }
  }

}
