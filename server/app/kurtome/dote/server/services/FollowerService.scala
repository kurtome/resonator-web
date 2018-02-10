package kurtome.dote.server.services

import javax.inject._

import kurtome.dote.server.db.FollowerDbIo
import kurtome.dote.shared.util.result.ActionStatus
import kurtome.dote.shared.util.result.UnknownErrorStatus
import kurtome.dote.shared.util.result.SuccessStatus
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

  def readFollowers(personId: Long): Future[Seq[Long]] = {
    db.run(followerDbIo.readByFollowee(personId)).map(_.map(_.followerId))
  }

  def readFollowees(personId: Long): Future[Seq[Long]] = {
    db.run(followerDbIo.readByFollower(personId)).map(_.map(_.followerId))
  }

}
