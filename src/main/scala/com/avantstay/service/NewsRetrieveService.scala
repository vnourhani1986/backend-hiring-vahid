package com.avantstay.service

import cats.effect.Concurrent
import com.avantstay.infrastructure.repo.HeadlineRepo
import com.avantstay.model.core.News
import com.avantstay.model.generic.Headline
import io.circe.Json
import sangria.ast.Document
import sangria.execution.Executor
import sangria.macros.derive._
import sangria.marshalling.circe._
import sangria.schema.{Field, ListType, ObjectType, Schema, fields}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class NewsRetrieveService[F[_]: Concurrent](
    headlineRepo: HeadlineRepo[F]
)(implicit
    executionContext: ExecutionContext
) {

  import NewsRetrieveService._

  private val NewsType: ObjectType[Unit, News] =
    deriveObjectType[Unit, News](ObjectTypeDescription("news headlines"))

  private val QueryType: ObjectType[HeadlineRepo[F], Unit] = ObjectType(
    "Query",
    fields[HeadlineRepo[F], Unit](
      Field(
        "news",
        ListType(NewsType),
        description = Some("Returns a list of all news"),
        resolve = _.ctx.get.map(headlineToNews)
      )
    )
  )

  private val schema: Schema[HeadlineRepo[F], Unit] = Schema(QueryType)

  def get(query: Document): F[Json] =
    Concurrent[F].async { f: (Either[Throwable, Json] => Unit) =>
      Executor.execute(schema, query, headlineRepo).onComplete {
        case Success(value)     => f(Right(value))
        case Failure(exception) => f(Left(exception))
      }
    }

}

object NewsRetrieveService {

  def apply[F[_]: Concurrent](
      headlineRepo: HeadlineRepo[F]
  )(implicit
      executionContext: ExecutionContext
  ): NewsRetrieveService[F] = new NewsRetrieveService[F](headlineRepo)

  private val headlineToNews: Headline => News =
    headline =>
      News(
        title = headline.title,
        link = headline.link
      )

}
