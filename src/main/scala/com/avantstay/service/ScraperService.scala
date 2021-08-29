package com.avantstay.service

import cats.effect.Concurrent
import com.avantstay.infrastructure.client.ScraperClients
import com.avantstay.infrastructure.repo.HeadlineRepo
import com.avantstay.model.generic.Headline
import net.ruippeixotog.scalascraper.model.Document

sealed trait ScraperService[F[_]] {
  protected def get: F[Document]
  protected def parse(document: Document): F[Seq[Headline]]
  protected def persist(headlines: Seq[Headline]): F[Boolean]
  def scrape: F[Boolean]
}

class ScraperNyTimesService[F[_]: Concurrent](
    headlineRepo: HeadlineRepo[F],
    scraperClients: ScraperClients[F]
) extends ScraperService[F] {
  override def get: F[Document] = scraperClients.get
  override def parse(document: Document): F[Seq[Headline]] = ???
  override def persist(headlines: Seq[Headline]): F[Boolean] = ???
  override def scrape: F[Boolean] = ???
}
