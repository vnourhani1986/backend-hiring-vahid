package com.avantstay

import cats.effect.{Blocker, Concurrent, Timer}
import com.avantstay.infrastructure.client.ScraperNYTimesClients
import com.avantstay.infrastructure.endpoint.NewsEndpoint
import com.avantstay.infrastructure.repo.HeadlinePostgresRepo
import com.avantstay.service.{NewsRetrieveServiceImpl, ScraperNyTimesService}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.{Task, URIO}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object ZioBaseService extends zio.interop.catz.CatsApp {

  implicit val timer: Timer[Task] = ioTimer

  override def run(args: List[String]): URIO[zio.ZEnv, zio.ExitCode] =
    (for {
      nonBlockingPool <- Task(Executors.newFixedThreadPool(4))
      nonBlockingContext <- Task(ExecutionContext.fromExecutor(nonBlockingPool))
      blockingPool <- Task(Executors.newFixedThreadPool(4))
      blockingContext <- Task(Blocker.liftExecutorService(blockingPool))
      loadedServiceConfig <- ServiceConfig.load[Task](blockingContext)
      urlsConfig <- Task(loadedServiceConfig.client.api.urls)
      scraperClients <- Task(ScraperNYTimesClients[Task](urlsConfig.nytimes))
      headlinePostgresRepo <- Task(HeadlinePostgresRepo[Task])
      _ <-
        ScraperNyTimesService[Task](scraperClients, headlinePostgresRepo).scrape
      newsRetrieveService <- Task(
        NewsRetrieveServiceImpl[Task](headlinePostgresRepo)(
          implicitly[Concurrent[Task]],
          nonBlockingContext
        )
      )
      newsEndpoint <- Task(
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
    } yield server).exitCode

// todo: 2. writing some tests
// todo: 3. adding zio and monix data types
}
