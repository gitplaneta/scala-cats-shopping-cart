package shop.http.routes

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import shop.services.HealthCheck
import org.http4s.circe.CirceEntityCodec._
import org.http4s.server.Router

case class HealthRoutes[F[_]: Monad](
    healthCheck: HealthCheck[F]
) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/healthcheck"

  private val httpRoutes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok(healthCheck.status)
    }

  val routes = Router(
    prefixPath -> httpRoutes
  )
}
