package kurtome.dote.server.util

import kurtome.dote.server.util.UrlIds.IdKinds
import org.scalatest.WordSpec

class UrlIdsTest extends WordSpec {

  "encode" when {
    "single digit ID" should {
      "be length 6" in {
        assert(UrlIds.encode(IdKinds.Dotable, 1).size === 6)
      }
      "encode 1 the same each time" in {
        assert(UrlIds.encode(IdKinds.Dotable, 1) === "aMxhJq")
        assert(UrlIds.encode(IdKinds.Dotable, 1) === "aMxhJq")
      }
      "encode 2 the same each time" in {
        assert(UrlIds.encode(IdKinds.Dotable, 2) === "aDrhrX")
        assert(UrlIds.encode(IdKinds.Dotable, 2) === "aDrhrX")
      }
      "encode 3 the same each time" in {
        assert(UrlIds.encode(IdKinds.Dotable, 3) === "qZDhGE")
        assert(UrlIds.encode(IdKinds.Dotable, 3) === "qZDhGE")
      }
    }
    "Long ID" should {
      val longId: Long = Int.MaxValue.toLong + 1L
      "be length 9" in {
        assert(UrlIds.encode(IdKinds.Dotable, longId).size === 9)
      }
      "Long ID the same each time" in {
        assert(UrlIds.encode(IdKinds.Dotable, longId) === "16hrN66Ay")
        assert(UrlIds.encode(IdKinds.Dotable, longId) === "16hrN66Ay")
      }
    }
  }

  "decode" when {
    "single digit ID" should {
      "decode 1 from constant" in {
        assert(UrlIds.decode(IdKinds.Dotable, "aMxhJq") === 1)
      }
    }
    "reflexive with encode" should {
      "514356658" in {
        assert(
          UrlIds.decode(IdKinds.Dotable, UrlIds.encode(IdKinds.Dotable, 514356658)) === 514356658)
      }
      "9" in {
        assert(UrlIds.decode(IdKinds.Dotable, UrlIds.encode(IdKinds.Dotable, 9)) === 9)
      }
      "100" in {
        assert(UrlIds.decode(IdKinds.Dotable, UrlIds.encode(IdKinds.Dotable, 100)) === 100)
      }
    }
  }

}
