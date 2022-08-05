package shop.http.routes.admin

import cats.MonadThrow
import cats.syntax.all._
import io.circe.JsonObject
import io.circe.syntax.EncoderOps
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRoutes, HttpRoutes}
import shop.domain.item.{CreateItemParam, UpdateItemParam}
import shop.ext.http4s.refined._
import shop.http.auth.users.AdminUser
import shop.services.Items

final case class AdminItemRoutes[F[_]: JsonDecoder: MonadThrow](
    items: Items[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/items"

  private val httpRoutes = AuthedRoutes.of[AdminUser, F] {
    case ar @ POST -> Root as _ =>
      ar.req.decodeR[CreateItemParam] { item =>
        items
          .create(item.toDomain)
          .flatMap(id => Created(JsonObject.singleton("item_id", id.asJson)))

      }
    case ar @ PUT -> Root as _ =>
      ar.req.decodeR[UpdateItemParam](item => items.update(item.toDomain) >> Ok())
  }

  def routes(authMiddleware: AuthMiddleware[F, AdminUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
