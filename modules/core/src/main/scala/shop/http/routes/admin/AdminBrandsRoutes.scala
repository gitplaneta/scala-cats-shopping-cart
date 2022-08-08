package shop.http.routes.admin

import cats.MonadThrow
import io.circe.JsonObject
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}
import shop.domain.brand.BrandParam
import shop.ext.http4s.refined.RefinedRequestDecoder
import shop.http.auth.users.AdminUser
import cats.syntax.all._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import shop.services.Brands

final case class AdminBrandsRoutes[F[_]: JsonDecoder: MonadThrow](
    brands: Brands[F]
) extends Http4sDsl[F] {

  private val prefixPath = "/brands"

  private val httpRoutes = AuthedRoutes.of[AdminUser, F] {
    case ar @ POST -> Root as _ =>
      ar.req.decodeR[BrandParam] { bp =>
        brands.create(bp.toDomain).flatMap { id =>
          Created(JsonObject.singleton("brand_id", id.asJson))
        }
      }
  }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
