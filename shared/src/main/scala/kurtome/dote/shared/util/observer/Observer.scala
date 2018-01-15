package kurtome.dote.shared.util.observer

import scala.collection.mutable.ListBuffer

trait Observer[S] {
  def receiveUpdate(subject: S)
}

trait Observable[S] {
  val observers: ListBuffer[Observer[S]] = ListBuffer()
  def addObserver(observer: Observer[S]) = observers += observer
  def removeObserver(observer: Observer[S]) = observers -= observer
  def notifyObservers(state: S) = observers.foreach(_.receiveUpdate(state))
}

class SimpleObservable[S]() extends Observable[S] {}
object SimpleObservable {
  def apply[S]() = new SimpleObservable[S]()
}
