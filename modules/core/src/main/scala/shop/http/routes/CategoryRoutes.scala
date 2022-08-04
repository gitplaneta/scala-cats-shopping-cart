package shop.http.routes

import cats.Monad
import shop.services.Categories
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.server.Router

final case class CategoryRoutes[F[_]: Monad](
    category: Categories[F]
) extends Http4sDsl[F] {

  private[routes] val prefix = "/categories"

  val httpRoutes = HttpRoutes.of[F] {
    case GET -> Root => Ok(category.findAll)
  }

  val routes = Router(
    prefix -> httpRoutes
  )
}
