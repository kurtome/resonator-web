package resonator.server.services

import javax.inject._
import resonator.server.db.DotableTagDbIo
import resonator.shared.constants.TagKinds.TagKind
import resonator.slick.db.gen.Tables
import slick.basic.BasicBackend

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class TagService @Inject()(db: BasicBackend#Database, dotableTagDbIo: DotableTagDbIo)(
    implicit executionContext: ExecutionContext) {

  def readTags(kind: TagKind, keys: Seq[String]): Future[Seq[Tables.TagRow]] = {
    for {
      tags <- db.run(dotableTagDbIo.readTagsByKeys(kind, keys))
    } yield tags
  }

}
