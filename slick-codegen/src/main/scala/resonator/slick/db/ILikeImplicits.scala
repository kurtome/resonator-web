package resonator.slick.db

import slick.jdbc.PostgresProfile.api._

/**
  * Adds support to the slick API for "ilike" queries
  */
trait ILikeImplicits {

  implicit class IlikeOps(s: Rep[String]) {
    def ilike(p: Rep[String]): Rep[Boolean] = {
      val expr = SimpleExpression.binary[String, String, Boolean] { (s, p, qb) =>
        qb.expr(s)
        qb.sqlBuilder += " ILIKE "
        qb.expr(p)
      }
      expr.apply(s, p)
    }
  }

  implicit class IlikeOptOps(s: Rep[Option[String]]) {
    def ilike(p: Rep[String]): Rep[Boolean] = {
      val expr = SimpleExpression.binary[Option[String], String, Boolean] { (s, p, qb) =>
        qb.expr(s)
        qb.sqlBuilder += " ILIKE "
        qb.expr(p)
      }
      expr.apply(s, p)
    }
  }
}
