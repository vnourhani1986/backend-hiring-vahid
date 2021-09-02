package com.avantstay.infrastructure.client

import cats.effect.Sync
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.model.Document

sealed trait ScraperClients[F[_]] {
  def get: F[Document]
}

class ScraperNYTimesClients[F[_]: Sync](url: String, browser: Browser)
    extends ScraperClients[F] {

  private val F: cats.effect.Sync[F] = implicitly

  override def get: F[Document] =
    F.delay(browser.get(url))
}

object ScraperNYTimesClients {
  def apply[F[_]: Sync](url: String): ScraperNYTimesClients[F] =
    new ScraperNYTimesClients[F](url, JsoupBrowser())
}
