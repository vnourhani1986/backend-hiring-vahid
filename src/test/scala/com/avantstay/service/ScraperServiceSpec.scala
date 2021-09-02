package com.avantstay.service

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.avantstay.infrastructure.client.ScraperClients
import com.avantstay.infrastructure.repo.HeadlineRepo
import com.avantstay.model.generic.Headline
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.mockito.Mockito._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class ScraperServiceSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  implicit val ec: ExecutionContext = executionContext

  "parse method" - {
    "should parse docs and return all headlines in it" in {
      val browser = JsoupBrowser()
      val doc = browser.parseFile("src/test/resources/nytimes.html")
      ScraperNyTimesService[IO](null, null)
        .parse(doc)
        .asserting(
          _ shouldBe Seq(
            Headline(
              "Lightning Strike Kills Teenage Lifeguard in New Jersey",
              "https://www.nytimes.com/2021/08/31/nyregion/jersey-shore-lightning-strike-lifeguard-dead.html"
            )
          )
        )
    }
  }

  "scrape method" - {
    "should parse docs and return all headlines in it" in {

      val scraperClients = mock(classOf[ScraperClients[IO]])
      val headlineRepo = mock(classOf[HeadlineRepo[IO]])
      val scraperService =
        ScraperNyTimesService[IO](scraperClients, headlineRepo)

      val browser = JsoupBrowser()
      val doc = browser.parseFile("src/test/resources/nytimes.html")
      val headline = Headline(
        "Lightning Strike Kills Teenage Lifeguard in New Jersey",
        "https://www.nytimes.com/2021/08/31/nyregion/jersey-shore-lightning-strike-lifeguard-dead.html"
      )

      when(scraperClients.get).thenReturn(IO(doc))
      when(
        headlineRepo.save(
          Seq(headline)
        )
      ).thenReturn(IO(true))

      scraperService.scrape.unsafeRunSync()
      verify(scraperClients).get
      verify(headlineRepo).save(Seq(headline))
      true shouldBe true // fake asserting to complete the test
    }
  }
}
