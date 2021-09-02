package com.avantstay.infrastructure.repo

import cats.effect.Concurrent
import com.avantstay.infrastructure.repo.model.Headlines
import com.avantstay.model.generic.Headline
import io.getquill.{PostgresJdbcContext, SnakeCase}

sealed trait HeadlineRepo[F[_]] {
  def get: Seq[Headline]
  def save(headlines: Seq[Headline]): F[Boolean]
}

class HeadlinePostgresRepo[F[_]: Concurrent](
    ctx: PostgresJdbcContext[SnakeCase.type]
) extends HeadlineRepo[F] {

  private val F: cats.effect.Concurrent[F] = implicitly

  import ctx._

  override def get: Seq[Headline] =
    ctx
      .run(query[Headlines])
      .map(headlines => Headline(headlines.title, headlines.link))

  override def save(headlines: Seq[Headline]): F[Boolean] = {
    val insertQuote = quote {
      liftQuery(headlines).foreach(headline =>
        query[Headlines].insert(
          Headlines(headline.title, headline.link)
        ).onConflictIgnore
      )
    }
    F.delay(!ctx.run(insertQuote).contains(0))
  }
}

object HeadlinePostgresRepo {
  def apply[F[_]: Concurrent]: HeadlinePostgresRepo[F] =
    new HeadlinePostgresRepo[F](
      new PostgresJdbcContext(SnakeCase, "postgres-db")
    )
}
