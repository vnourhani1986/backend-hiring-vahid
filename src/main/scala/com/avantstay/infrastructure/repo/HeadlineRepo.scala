package com.avantstay.infrastructure.repo

import cats.effect.Concurrent
import com.avantstay.model.generic.Headline

sealed trait HeadlineRepo[F[_]] {
  def get: F[Seq[Headline]]
  def save(headline: Headline): F[Boolean]
}

class HeadlinePostgresRepo[F[_]: Concurrent] extends HeadlineRepo[F] {
  override def get: F[Seq[Headline]] = ???

  override def save(headline: Headline): F[Boolean] = ???
}
