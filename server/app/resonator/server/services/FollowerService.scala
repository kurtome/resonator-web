package resonator.server.services

import javax.inject._
import resonator.server.db.FollowerDbIo
import resonator.shared.util.result.ActionStatus
import resonator.shared.util.result.UnknownErrorStatus
import resonator.shared.util.result.SuccessStatus
import resonator.slick.db.gen.Tables
import slick.basic.BasicBackend

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class FollowerService @Inject()(db: BasicBackend#Database, followerDbIo: FollowerDbIo)(
    implicit executionContext: ExecutionContext) {

  def follow(followerId: Long, followeeId: Long): Future[ActionStatus] = {
    db.run(followerDbIo.insert(followerId, followeeId))
      .map(_ => SuccessStatus) recover {
      case t: Throwable => UnknownErrorStatus
    }
  }

  def unfollow(followerId: Long, followeeId: Long): Future[ActionStatus] = {
    db.run(followerDbIo.delete(followerId, followeeId)).map(_ => SuccessStatus) recover {
      case t: Throwable => UnknownErrorStatus
    }
  }

  def readFollowers(personId: Long): Future[Seq[Tables.PersonRow]] = {
    db.run(followerDbIo.readByFollowee(personId)).map(_.map(_._2))
  }

  def readFollowees(personId: Long): Future[Seq[Tables.PersonRow]] = {
    db.run(followerDbIo.readByFollower(personId)).map(_.map(_._2))
  }

}
