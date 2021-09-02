package com.avantstay.endpoint

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.avantstay.infrastructure.endpoint.NewsEndpoint
import com.avantstay.service.NewsRetrieveService
import io.circe.parser._
import org.http4s.Response
import org.http4s.Status.BadRequest
import org.http4s.dsl.io.Ok
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import sangria.ast.Document

import scala.concurrent.ExecutionContext

class NewsEndpointSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  implicit val ec: ExecutionContext = executionContext

  "graphQlEndpoint method" - {
    "should call get method of newsRetrieveService" in {

      val requestJson = parse(
        """{"query":"{news{link, title}}"}"""
      ).toOption.get
      val responseJson = parse(
        """ { "query": { "news": [{ "link": "link" , "title": "title" }]}}"""
      ).toOption.get

      val newsRetrieveService = mock(classOf[NewsRetrieveService[IO]])
      when(newsRetrieveService.get(any[Document])).thenReturn(IO(responseJson))

      NewsEndpoint[IO](newsRetrieveService)
        .graphQLEndpoint(requestJson)
        .unsafeRunSync()

      verify(newsRetrieveService).get(any[Document])
      true shouldBe true // fake assertion for complete the test
    }
  }
  "graphQlEndpoint method" - {
    "should return OK response with correct query" in {

      val requestJson = parse(
        """{"query" : "{ news {link, title} }"}"""
      ).toOption.get
      val responseJson = parse(
        """{"query": { "news": [{ "link": "link" , "title": "title" }]}}""".stripMargin
      ).toOption.get

      val newsRetrieveService = mock(classOf[NewsRetrieveService[IO]])
      when(newsRetrieveService.get(any[Document])).thenReturn(IO(responseJson))

      val newsEndpoint = NewsEndpoint[IO](newsRetrieveService)

      newsEndpoint
        .graphQLEndpoint(requestJson)
        .asserting(
          _.status shouldBe Response[IO](status = Ok).status
        )
    }
  }
  "graphQlEndpoint method" - {
    "should return BadRequest response with incorrect request body" in {

      val requestJson = parse(
        """{"q" : "{ news {link, title} }"}"""
      ).toOption.get
      val responseJson = parse(
        """ { "error": "query field not found"}"""
      ).toOption.get

      val newsEndpoint = NewsEndpoint[IO](null)

      newsEndpoint
        .graphQLEndpoint(requestJson)
        .asserting(
          _.status shouldBe Response[IO](status = BadRequest).status
        )

    }
  }
}
