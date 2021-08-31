package com.avantstay.infrastructure.client

import cats.effect.Concurrent
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.model.Document

sealed trait ScraperClients[F[_]] {
  def get: F[Document]
}

class ScraperNYTimesClients[F[_]: Concurrent](url: String, browser: Browser)
    extends ScraperClients[F] {
  override def get: F[Document] =
    Concurrent[F].delay(browser.get(url))
}

object ScraperNYTimesClients {
  def apply[F[_]: Concurrent](url: String): ScraperNYTimesClients[F] =
    new ScraperNYTimesClients[F](url, JsoupBrowser())
}
