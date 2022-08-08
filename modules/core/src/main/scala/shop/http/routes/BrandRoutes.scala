package shop.http.routes

import cats.Monad
import shop.domain.brand
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s._
import org.http4s.server.Router
import shop.services.Brands

final case class BrandRoutes[F[_]: Monad](
    brands: Brands[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/brands"

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "brands" => Ok(brands.findAll)
  }

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)

}
