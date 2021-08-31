package com.avantstay.infrastructure.endpoint

import cats.effect.Concurrent
import cats.syntax.all._
import com.avantstay.service.NewsRetrieveService
import io.circe.{HCursor, Json}
import org.http4s.Method.POST
import org.http4s.Status.BadRequest
import org.http4s.circe.{jsonEncoder, jsonOf}
import org.http4s.dsl.io.{->, /, Ok, Root}
import org.http4s.{EntityDecoder, HttpRoutes, Response}
import sangria.parser.QueryParser

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class NewsEndpoint[F[_]: Concurrent](
    newsRetrieveService: NewsRetrieveService[F]
) {

  implicit val decoder: EntityDecoder[F, Json] = jsonOf[F, Json]

  def routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case request @ POST -> Root / "news" =>
        for {
          body <- request.as[Json]
          response <- graphQLEndpoint(body)
        } yield response
    }

  def graphQLEndpoint(json: Json): F[Response[F]] = {
    val cursor: HCursor = json.hcursor
    val query =
      cursor.downField("query").as[String].toOption.get // todo: need to manage

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        newsRetrieveService.get(queryAst).map { json =>
          Response[F](status = Ok).withEntity(json)
        }
      case Failure(error) =>
        Concurrent[F].delay(
          Response[F](status = BadRequest)
            .withEntity(Json.obj("error" -> Json.fromString(error.getMessage)))
        )
    }
  }

}

object NewsEndpoint {
  def apply[F[_]: Concurrent](
      newsRetrieveService: NewsRetrieveService[F]
  )(implicit
      executionContext: ExecutionContext
  ): NewsEndpoint[F] = new NewsEndpoint[F](newsRetrieveService)
}
