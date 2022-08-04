package shop.http.routes

import cats.Monad
import dev.profunktor.auth.AuthHeaders
import org.http4s.{ AuthedRoutes, HttpRoutes }
import org.http4s.dsl.Http4sDsl
import shop.http.auth.users.CommonUser
import shop.services.Auth
import cats.syntax.all._
import shop.http.auth.users._
import org.http4s.server.{ AuthMiddleware, Router }

final case class LogoutRoutes[F[_]: Monad](
    auth: Auth[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/auth"

  private val httpRoutes: AuthedRoutes[CommonUser, F] = AuthedRoutes.of {
    case ar @ POST -> Root / "logout" as user =>
      AuthHeaders
        .getBearerToken(ar.req)
        .traverse_(t => auth.logout(t, user.value.name)) *>
        NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, CommonUser]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
