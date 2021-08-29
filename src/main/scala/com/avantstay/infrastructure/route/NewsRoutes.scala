package com.avantstay.infrastructure.route

import cats.effect.Concurrent
import org.http4s.HttpRoutes

class NewsRoutes[F[_]: Concurrent] {
  def routes: HttpRoutes[F] = ???
}

object NewsRoutes {
  def apply[F[_]: Concurrent]: NewsRoutes[F] = new NewsRoutes[F]
}
