package com.avantstay.infrastructure.repo

import cats.effect.Concurrent
import com.avantstay.model.generic.Headline
import io.getquill.{PostgresJdbcContext, SnakeCase}

sealed trait HeadlineRepo[F[_]] {
  def get: Seq[Headline]
  def save(headlines: Seq[Headline]): F[Boolean]
}
// todo: change models to repo model
class HeadlinePostgresRepo[F[_]: Concurrent](
    ctx: PostgresJdbcContext[SnakeCase.type]
) extends HeadlineRepo[F] {

  import ctx._

  override def get: Seq[Headline] = ctx.run(query[Headline]).map(x => x)

  override def save(headlines: Seq[Headline]): F[Boolean] = {
    val insertQuote = quote {
      liftQuery(headlines).foreach(headline => query[Headline].insert(headline))
    }
    Concurrent[F].delay(!ctx.run(insertQuote).contains(0))
  }
}

object HeadlinePostgresRepo {
  def apply[F[_]: Concurrent]: HeadlinePostgresRepo[F] =
    new HeadlinePostgresRepo[F](new PostgresJdbcContext(SnakeCase, "postgres-db"))
}
