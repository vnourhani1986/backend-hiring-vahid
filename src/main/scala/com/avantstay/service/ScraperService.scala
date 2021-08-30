package com.avantstay.service

import cats.effect.Concurrent
import cats.implicits._
import com.avantstay.infrastructure.client.{ScraperClients, ScraperNYTimesClients}
import com.avantstay.infrastructure.repo.HeadlineRepo
import com.avantstay.model.generic.Headline
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
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
  override def parse(document: Document): F[Seq[Headline]] =
    for {
      storyWrapper <- Concurrent[F].delay(document >> elementList("section a"))
      titleLinks <- Concurrent[F].delay(
        storyWrapper.map(e => (e >> text("h3"), e.attr("href")))
      )
    } yield titleLinks.map { case (title, link) => Headline(title, link) }
  override def persist(headlines: Seq[Headline]): F[Boolean] =
    headlineRepo.save(headlines)
  override def scrape: F[Boolean] =
    for {
      document <- get
      headlines <- parse(document)
      result <- persist(headlines)
    } yield result
}

object ScraperNyTimesService {
  def apply[F[_]: Concurrent](
      url: String
  )(headlineRepo: HeadlineRepo[F]): ScraperNyTimesService[F] =
    new ScraperNyTimesService[F](
      headlineRepo,
      ScraperNYTimesClients[F](url)
    )
}
