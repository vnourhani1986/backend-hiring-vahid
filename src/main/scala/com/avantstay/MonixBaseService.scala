package com.avantstay

import cats.effect.{Blocker, Concurrent, ExitCode}
import com.avantstay.infrastructure.client.ScraperNYTimesClients
import com.avantstay.infrastructure.endpoint.NewsEndpoint
import com.avantstay.infrastructure.repo.HeadlinePostgresRepo
import com.avantstay.service.{NewsRetrieveServiceImpl, ScraperNyTimesService}
import monix.bio.{IO, Task}
import monix.eval
import monix.eval._
import monix.execution.Scheduler.Implicits.global
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object MonixBaseService extends TaskApp {
  override def run(args: List[String]): eval.Task[ExitCode] =
    (for {
      nonBlockingPool <- IO(Executors.newFixedThreadPool(4))
      nonBlockingContext <- IO(ExecutionContext.fromExecutor(nonBlockingPool))
      blockingPool <- IO(Executors.newFixedThreadPool(4))
      blockingContext <- IO(Blocker.liftExecutorService(blockingPool))
      loadedServiceConfig <- ServiceConfig.load[Task](blockingContext)
      urlsConfig <- IO(loadedServiceConfig.client.api.urls)
      scraperClients <- IO(ScraperNYTimesClients[Task](urlsConfig.nytimes))
      headlinePostgresRepo <- IO(HeadlinePostgresRepo[Task])
      _ <-
        ScraperNyTimesService[Task](scraperClients, headlinePostgresRepo).scrape
      newsRetrieveService <- IO(
        NewsRetrieveServiceImpl[Task](headlinePostgresRepo)(
          implicitly[Concurrent[Task]],
          nonBlockingContext
        )
      )
      newsEndpoint <- IO(
        NewsEndpoint[Task](newsRetrieveService)(
          implicitly[Concurrent[Task]],
          nonBlockingContext
        )
      )
      server <-
        BlazeServerBuilder[Task](
          implicitly[ExecutionContext](nonBlockingContext)
        ).bindHttp(
            loadedServiceConfig.host.port,
            host = loadedServiceConfig.host.address
          )
          .withHttpApp(newsEndpoint.routes.orNotFound)
          .serve
          .compile
          .lastOrError
    } yield server).to[eval.Task]

// todo: 2. writing some tests
// todo: 3. adding zio and monix data types

}
