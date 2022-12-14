package shop.http.routes

import shop.domain.brand._
import shop.services.Items
import cats.Monad
import cats.effect.IO
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
//import shop.ext.http4s.refined._
import cats.syntax.all._

final case class ItemRoutes[F[_]: Monad](
    items: Items[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/items"

  object BrandQueryParam extends OptionalQueryParamDecoderMatcher[BrandParam]("brand")

  private val httpRoutes = HttpRoutes.of[F] {
    case GET -> Root :? BrandQueryParam(brand) =>
      Ok(brand.fold(items.findAll)(a => items.findBy(a.toDomain)))
  }

  val routes = Router(
    prefixPath -> httpRoutes
  )
}
