package com.avantstay.infrastructure.client

import cats.effect.Concurrent
import net.ruippeixotog.scalascraper.model.Document

sealed trait ScraperClients[F[_]] {
  def get: F[Document]
}

class ScraperNYTimesClients[F[_]: Concurrent] extends ScraperClients[F] {
  override def get: F[Document] = ???
}