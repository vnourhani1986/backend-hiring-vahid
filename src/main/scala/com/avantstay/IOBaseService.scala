package com.avantstay

import cats.effect.{Blocker, Concurrent, ExitCode, IO, IOApp}
import com.avantstay.infrastructure.endpoint.NewsEndpoint
import com.avantstay.infrastructure.repo.HeadlinePostgresRepo
import com.avantstay.service.{NewsRetrieveService, ScraperNyTimesService}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object IOBaseService extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      nonBlockingPool <- IO(Executors.newFixedThreadPool(4))
      nonBlockingContext <- IO(ExecutionContext.fromExecutor(nonBlockingPool))
      blockingPool <- IO(Executors.newFixedThreadPool(4))
      blockingContext <- IO(Blocker.liftExecutorService(blockingPool))
      loadedServiceConfig <- ServiceConfig.load[IO](blockingContext)
      urlsConfig <- IO(loadedServiceConfig.client.api.urls)
      headlinePostgresRepo <- IO(HeadlinePostgresRepo[IO])
      scraperService <- IO(
        ScraperNyTimesService[IO](urlsConfig.nytimes)(headlinePostgresRepo)
      )
//      _ <- scraperService.scrape // todo: need to define in correct place
      newsRetrieveService <- IO(
        NewsRetrieveService[IO](headlinePostgresRepo)(
          implicitly[Concurrent[IO]],
          nonBlockingContext
        )
      )
      newsEndpoint <- IO(
        NewsEndpoint[IO](newsRetrieveService)(
          implicitly[Concurrent[IO]],
          nonBlockingContext
        )
      )
      server <-
        BlazeServerBuilder[IO](implicitly[ExecutionContext](nonBlockingContext))
          .bindHttp(
            loadedServiceConfig.host.port,
            host = loadedServiceConfig.host.address
          )
          .withHttpApp(newsEndpoint.routes.orNotFound)
          .serve
          .compile
          .lastOrError
    } yield server
  }

}
