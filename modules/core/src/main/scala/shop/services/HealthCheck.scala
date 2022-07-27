package shop.services

import shop.domain.healthcheck.AppStatus

trait HealthCheck[F[_]] {
  def status: F[AppStatus]

}
