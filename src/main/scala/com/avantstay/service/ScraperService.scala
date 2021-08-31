package com.avantstay.service

import cats.effect.Sync
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

class ScraperNyTimesService[F[_]: Sync](
    headlineRepo: HeadlineRepo[F],
    scraperClients: ScraperClients[F]
) extends ScraperService[F] {
  override def get: F[Document] = scraperClients.get
  override def parse(document: Document): F[Seq[Headline]] =
    Sync[F].delay {
      val storyWrapperAH3 =
        document >> elementList("section .story-wrapper a h3")
      val (links, h3s) = (
        storyWrapperAH3.map(_.parent.get) >?> attr("href")("a"),
        storyWrapperAH3.map(_.text)
      )
      val titleLinks = links
        .zip(h3s)
        .filter { case (link, _) => link.isDefined }
        .map { case (link, h3) => (link.get, h3) }
      titleLinks.map { case (link, title) => Headline(title, link) }
    }

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
  def apply[F[_]: Sync](
      url: String
  )(headlineRepo: HeadlineRepo[F]): ScraperNyTimesService[F] =
    new ScraperNyTimesService[F](
      headlineRepo,
      ScraperNYTimesClients[F](url)
    )
}
