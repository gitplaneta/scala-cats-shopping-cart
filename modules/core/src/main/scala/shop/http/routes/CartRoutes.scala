package shop.http.routes

import cats.Monad
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.circe._
import cats.syntax.all._
import org.http4s.dsl.Http4sDsl
import shop.http.auth.users.CommonUser
import shop.services.ShoppingCart
import org.http4s.circe.CirceEntityCodec._
import org.http4s.server.{ AuthMiddleware, Router }
import shop.domain.cart.Cart
import shop.http.vars.ItemIdVar

final case class CartRoutes[F[_]: JsonDecoder: Monad](
    shoppingCart: ShoppingCart[F]
) extends Http4sDsl[F] {
  private[routes] val prefixPath = "/cart"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case GET -> Root as user => Ok(shoppingCart.get(user.value.id))

    case ar @ POST -> Root as user =>
      ar.req.asJsonDecode[Cart].flatMap { cart =>
        cart.items
          .map {
            case (id, q) => shoppingCart.add(user.value.id, id, q)
          }
          .toList
          .sequence *> Created()
      }

    case ar @ PUT -> Root as user =>
      ar.req.asJsonDecode[Cart].flatMap { c =>
        shoppingCart.update(user.value.id, c) *> Ok()
      }

    case DELETE -> Root / ItemIdVar(itemId) as user => shoppingCart.removeItem(user.value.id, itemId) *> NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
