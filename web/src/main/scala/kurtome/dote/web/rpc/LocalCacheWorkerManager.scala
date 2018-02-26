package kurtome.dote.web.rpc

import kurtome.dote.web.rpc.LocalCache.ObjectKinds.ObjectKind
import org.scalajs.dom
import org.scalajs.dom.webworkers.DedicatedWorkerGlobalScope

import scala.scalajs.js.JSConverters._
import org.scalajs.dom.webworkers.Worker
import com.trueaccord.scalapb.GeneratedMessage
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.api.feed.Feed
import kurtome.dote.web.rpc.LocalCache.ObjectKinds
import wvlet.log.LogLevel
import wvlet.log.LogSupport
import wvlet.log.Logger

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.LinkingInfo
import scala.util.Success

/**
  * Uses web worker to put [[LocalCache]] operations on another thread.
  */
object LocalCacheWorkerManager extends LogSupport {

  private val promiseMap: mutable.Map[String, Promise[js.Array[Byte]]] =
    mutable.Map[String, Promise[js.Array[Byte]]]()

  private var worker: Worker = null

  def initWorker(): Unit = {
    assert(worker == null)

    worker = new Worker("worker.js")

    worker.onmessage = (msg) => {
      val message = msg.data.asInstanceOf[ResponseMessage]
      message.cmd match {
        case "get" => {
          val get = message.asInstanceOf[GetResponse]
          debug(s"got ${get.mapKey}")
          promiseMap.get(get.mapKey).map(_.complete(Success(get.obj)))
          promiseMap.remove(get.mapKey)
        }
      }
    }
  }

  def get(kind: ObjectKind, key: String): Future[Option[Array[Byte]]] = {
    val mapKey = s"$kind$key"
    val existingPromise = promiseMap.get(mapKey)
    if (existingPromise.isDefined) {
      existingPromise.get.future.map(Some(_).map(_.toArray))
    } else {
      val promise = Promise[js.Array[Byte]]()
      promiseMap.put(mapKey, promise)
      worker.postMessage(new GetCommand(mapKey, kind.id, key))
      promise.future.map(Some(_).map(_.toArray))
    }
  }

  def put(kind: ObjectKind, key: String, obj: GeneratedMessage, cacheMinutes: Int = 10) = {
    worker.postMessage(new PutCommand(kind.id, key, obj.toByteArray.toJSArray, cacheMinutes))
  }
}

sealed trait RequestMessage extends js.Object {
  val cmd: String
}

private class GetCommand(val mapKey: String, val kind: Int, val key: String)
    extends RequestMessage {
  override val cmd = "get"
}

private class PutCommand(val kind: Int,
                         val key: String,
                         val obj: js.Array[Byte],
                         val cacheMinutes: Int = 10)
    extends RequestMessage {
  override val cmd = "put"
}

sealed trait ResponseMessage extends js.Object {
  val cmd: String
}

private class GetResponse(val mapKey: String, val obj: js.Array[Byte]) extends ResponseMessage {
  override val cmd = "get"
}

@JSExportTopLevel("AsyncLocalCacheWorker")
object AsyncLocalCacheWorker extends LogSupport {

  @JSExport
  def main() = {

    DedicatedWorkerGlobalScope.self.addEventListener("message", onMessage _)
    if (LinkingInfo.productionMode) {
      Logger.setDefaultLogLevel(LogLevel.WARN)
    } else {
      Logger.setDefaultLogLevel(LogLevel.DEBUG)
      debug("dev logging enabled.")
    }
  }

  def postMessage(msg: js.Any): Unit = {
    DedicatedWorkerGlobalScope.self.postMessage(msg)
  }

  def onMessage(msg: dom.MessageEvent) = {
    val message = msg.data.asInstanceOf[RequestMessage]
    message.cmd match {
      case "put" => {
        val put = message.asInstanceOf[PutCommand]
        val kind = LocalCache.ObjectKinds(put.kind)
        LocalCache.putObj(kind, put.key, put.obj, put.cacheMinutes)
        putChildren(kind, put.obj)
      }
      case "get" => {
        val get = message.asInstanceOf[GetCommand]
        LocalCache.getObj(LocalCache.ObjectKinds(get.kind), get.key) map { bytes =>
          if (bytes.isDefined) {
            debug("sending cached get")
            postMessage(new GetResponse(get.mapKey, bytes.get.toJSArray))
          } else {
            debug("miss cached get")
            postMessage(new GetResponse(get.mapKey, null))
          }
        }
      }

    }
  }

  private def putChildren(kind: ObjectKind, parentObj: js.Array[Byte]) = {
    kind match {
      case ObjectKinds.Feed => {
        Feed.parseFrom(parentObj.toArray).items foreach { item =>
          item.getDotableList.getList.dotables foreach { child =>
            LocalCache.putObj(ObjectKinds.DotableShallow, child.id, child.toByteArray.toJSArray)
          }
        }
      }
      case ObjectKinds.DotableDetails => {
        Dotable.parseFrom(parentObj.toArray).getRelatives.children foreach { child =>
          LocalCache.putObj(ObjectKinds.DotableShallow, child.id, child.toByteArray.toJSArray)
        }

      }
      case _ => Unit // Other types don't have children
    }
  }

}
