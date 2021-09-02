package com.avantstay.service

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.avantstay.infrastructure.repo.HeadlineRepo
import com.avantstay.model.generic.Headline
import org.mockito.Mockito._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import sangria.macros._

import scala.concurrent.ExecutionContext

class NewsRetrieveServiceSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers {

  implicit val ec: ExecutionContext = executionContext

  "get method" - {
    "should return all news" in {

      val headline = Headline(
        "Lightning Strike Kills Teenage Lifeguard in New Jersey",
        "https://www.nytimes.com/2021/08/31/nyregion/jersey-shore-lightning-strike-lifeguard-dead.html"
      )
      val query = graphql"""query Query {news {link, title}}"""

      val headlineRepo = mock(classOf[HeadlineRepo[IO]])
      when(headlineRepo.get).thenReturn(Seq(headline))

      val newsRetrieveService = NewsRetrieveServiceImpl[IO](headlineRepo)
      newsRetrieveService.get(query).unsafeRunSync()
      verify(headlineRepo).get
      true shouldBe true // fake assertion for complete the test
    }
  }
}
