package com.avantstay.service

import cats.effect.Concurrent
import com.avantstay.infrastructure.repo.HeadlineRepo
import com.avantstay.model.core.News

class NewsRetrieveService[F[_]: Concurrent](headlineRepo: HeadlineRepo[F]) {
  def get: F[News] = ???
}
