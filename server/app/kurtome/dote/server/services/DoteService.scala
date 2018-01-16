package kurtome.dote.server.services

import javax.inject._

import kurtome.dote.proto.api.dote.Dote
import kurtome.dote.server.db.DoteDbIo
import slick.basic.BasicBackend

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DoteService @Inject()(db: BasicBackend#Database, doteDbIo: DoteDbIo)(
    implicit executionContext: ExecutionContext) {

  def writeDote(personId: Long, dotableId: Long, dote: Dote): Future[Unit] = {
    db.run(doteDbIo.upsert(personId, dotableId, dote)).map(_ => Unit)
  }
}
