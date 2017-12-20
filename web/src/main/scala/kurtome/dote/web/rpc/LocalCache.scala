package kurtome.dote.web.rpc

import com.trueaccord.scalapb.GeneratedMessage
import dote.proto.api.dotable.Dotable
import kurtome.dote.web.rpc.LocalCache.ObjectKinds.ObjectKind
import wvlet.log.LogSupport

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSImport, JSName}

object LocalCache extends LogSupport {

  /**
    * http://pieroxy.net/blog/pages/lz-string/index.html
    */
  @JSImport("lz-string/libs/lz-string.js", JSImport.Default)
  @js.native
  object LzString extends js.Object {

    def compressToUTF16(value: String): String = js.native

    def decompressFromUTF16(value: String): String = js.native

  }

  /**
    * https://github.com/pamelafox/lscache
    */
  @JSImport("lscache/lscache.js", JSImport.Default)
  @js.native
  object LsCache extends js.Object {

    def set(key: String, value: String, time: js.UndefOr[Int] = js.undefined): String = js.native

    def set(key: String, value: js.Dynamic): String = js.native
    def set(key: String, value: js.Dynamic, time: Int): String = js.native

    def get(key: String): String = js.native

    @JSName("get")
    def getObj(key: String): js.Dynamic = js.native

    def remove(key: String): Unit = js.native

    def flush(): Unit = js.native

    def setBucket(bucket: String): Unit = js.native
  }

  object ObjectKinds extends Enumeration {
    type ObjectKind = Value
    val DotableShallow = Value("dotable-details")
    val DotableDetails = Value("dotable-shallow")
    val Feed = Value("feed")
  }

  def flushAll(): Unit = {
    ObjectKinds.values foreach { kind =>
      LsCache.setBucket(kind.toString)
      LsCache.flush()
    }
  }

  def getObj[R](kind: ObjectKind, key: String, parser: (Array[Byte]) => R): Option[R] = {
    LsCache.setBucket(kind.toString)
    Option(LsCache.get(key)) map { compressedOut =>
      val stringifiedBytesOut = LzString.decompressFromUTF16(compressedOut)
      val jsArrayOut = JSON.parse(stringifiedBytesOut)
      val bytesOut = jsArrayOut.asInstanceOf[js.Array[Byte]].toArray
      parser(bytesOut)
    }
  }

  def putObj(kind: ObjectKind, key: String, obj: GeneratedMessage, cacheMinutes: Int = 10): Unit = {
    val bytes = obj.toByteArray
    val stringifiedBytes = JSON.stringify(bytes.toJSArray)
    val compressed = LzString.compressToUTF16(stringifiedBytes)

    LsCache.setBucket(kind.toString)
    LsCache.set(key, compressed, cacheMinutes)
  }

  def put(includesDetails: Boolean, dotable: Dotable): Unit = {
    val kind = if (includesDetails) {
      ObjectKinds.DotableDetails
    } else {
      ObjectKinds.DotableShallow
    }
    putObj(kind, dotable.id, dotable)
  }

  def get(includesDetails: Boolean, dotableId: String): Option[Dotable] = {
    val kind = if (includesDetails) {
      ObjectKinds.DotableDetails
    } else {
      ObjectKinds.DotableShallow
    }
    getObj(kind, dotableId, Dotable.parseFrom)
  }

  private def hex2bytes(hex: String): Array[Byte] = {
    hex.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  private def bytes2hex(bytes: Array[Byte]): String = {
    bytes.map("%02x".format(_)).mkString
  }
}
