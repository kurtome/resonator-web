package kurtome.dote.web.rpc

import dote.proto.api.dotable.Dotable

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSImport, JSName}

object LocalCache {

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

  def flushAll(): Unit = {
    setBucket(true)
    LsCache.flush()
    setBucket(false)
    LsCache.flush()
  }

  def put(includesDetails: Boolean, dotable: Dotable): Unit = {
    val bytes = dotable.toByteArray
    val stringifiedBytes = JSON.stringify(bytes.toJSArray)
    val compressed = LzString.compressToUTF16(stringifiedBytes)
    val timeMinutes = 10

    setBucket(includesDetails)
    LsCache.set(dotable.id, compressed, timeMinutes)
  }

  def get(includesDetails: Boolean, dotableId: String): Option[Dotable] = {
    setBucket(includesDetails)
    Option(LsCache.get(dotableId)) map { compressedOut =>
      val stringifiedBytesOut = LzString.decompressFromUTF16(compressedOut)
      val jsArrayOut = JSON.parse(stringifiedBytesOut)
      val bytesOut = jsArrayOut.asInstanceOf[js.Array[Byte]].toArray
      Dotable.parseFrom(bytesOut)
    }
  }

  private def setBucket(includesDetails: Boolean) = {
    if (includesDetails) {
      LsCache.setBucket("dotable-details")
    } else {
      LsCache.setBucket("dotable-shallow")
    }
  }

  def hex2bytes(hex: String): Array[Byte] = {
    hex.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  def bytes2hex(bytes: Array[Byte]): String = {
    bytes.map("%02x".format(_)).mkString
  }
}
