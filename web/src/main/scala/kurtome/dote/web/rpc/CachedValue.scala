package kurtome.dote.web.rpc

import scala.scalajs.js.Date

sealed trait CachedValue[+T] {
  def get: Option[T]
}

object EmptyCachedValue extends CachedValue[Nothing] {
  override val get = None
}

case class TimeCachedValue[T](expiresAtTime: Double, value: T) extends CachedValue[T] {
  override def get: Option[T] = {
    if (expiresAtTime <= Date.now()) {
      None
    } else {
      Some(value)
    }
  }
}