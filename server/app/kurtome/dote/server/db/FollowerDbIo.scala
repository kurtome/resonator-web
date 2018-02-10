package kurtome.dote.server.db

import javax.inject._

import kurtome.dote.slick.db.DotePostgresProfile.api._
import kurtome.dote.slick.db.gen.Tables
import slick.lifted.Compiled

import scala.concurrent.ExecutionContext

@Singleton
class FollowerDbIo @Inject()(implicit executionContext: ExecutionContext) {
  object Queries {
    val filterByFollower = Compiled { (followerId: Rep[Long]) =>
      Tables.Follower.filter(_.followerId === followerId)
    }

    val filterByFollowee = Compiled { (followeeId: Rep[Long]) =>
      Tables.Follower.filter(_.followeeId === followeeId)
    }

    val delete = Compiled { (followeeId: Rep[Long], followerId: Rep[Long]) =>
      Tables.Follower
        .filter(row => row.followeeId === followeeId && row.followerId === followerId)
    }

  }

  def readByFollowee(followeeId: Long) = {
    Queries.filterByFollowee(followeeId).result
  }

  def readByFollower(followerId: Long) = {
    Queries.filterByFollower(followerId).result
  }

  def delete(followerId: Long, followeeId: Long) = {
    Queries.delete(followerId, followeeId).delete
  }

  def insert(followerId: Long, followeeId: Long) = {
    sqlu"""INSERT INTO follower
           (follow_time, follower_id, followee_id)
           SELECT now(), $followerId, $followeeId
           WHERE NOT EXISTS (
             SELECT 1
             FROM follower f
             WHERE f.follower_id = $followerId AND f.followee_id = $followeeId
           )
           """
  }

}
