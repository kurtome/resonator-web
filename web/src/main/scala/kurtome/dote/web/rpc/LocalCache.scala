package kurtome.dote.web.rpc

import kurtome.dote.web.rpc.LocalCache.ObjectKinds.ObjectKind
import kurtome.dote.web.utils.PerfTime
import wvlet.log.LogSupport

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.Promise
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.UndefOr.undefOr2ops
import scala.scalajs.js.annotation.JSImport

object LocalCache extends LogSupport {

  /**
    * https://github.com/jakearchibald/idb-keyval
    */
  @JSImport("idb-keyval", JSImport.Default)
  @js.native
  object IdbKeyval extends js.Object {

    /**
      * Stored values must be supported by structured cloning.
      */
    def set(key: String, value: js.Any): Promise[Unit] = js.native
    def get(key: String): Promise[js.UndefOr[js.Any]] = js.native
    def keys(): Promise[js.Array[String]] = js.native
    def delete(key: String): Promise[Unit] = js.native
    def clear(): Promise[Unit] = js.native
  }

  object ObjectKinds extends Enumeration {
    type ObjectKind = Value
    val DotableShallow = Value("dotable-shallow")
    val DotableDetails = Value("dotable-details")
    val Feed = Value("feed")
  }

  private class CacheRecord(val expiresAtTime: Double, val value: js.Any) extends js.Object

  private def createCacheKey(kind: ObjectKind, key: String): String = {
    s"resonator-cache/$kind/$key"
  }

  def flushAll(): Unit = {
    IdbKeyval.clear()
  }

  def getObj(kind: ObjectKind, key: String): Future[Option[Array[Byte]]] = {
    val cacheKey = createCacheKey(kind, key)
    IdbKeyval.get(cacheKey).toFuture map { value =>
      if (value.isDefined) {
        val cacheRecord = value.get.asInstanceOf[CacheRecord]
        if (cacheRecord.expiresAtTime > Date.now()) {
          val bytes = cacheRecord.value.asInstanceOf[js.Array[Byte]].toArray
          Some(bytes)
        } else {
          None
        }
      } else {
        None
      }
    }
  }

  def putObj(kind: ObjectKind,
             key: String,
             obj: js.Array[Byte],
             cacheMinutes: Int = 10): Future[Unit] = {
    val expiresAtTime = Date.now() + (cacheMinutes * 60 * 1000)
    val cacheKey = createCacheKey(kind, key)
    val cacheRecord = new CacheRecord(expiresAtTime, obj)
    IdbKeyval.set(cacheKey, cacheRecord).toFuture
  }

}
