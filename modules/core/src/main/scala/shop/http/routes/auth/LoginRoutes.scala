package shop.http.routes.auth

import cats.MonadThrow
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.domain.auth.{ InvalidPassword, LoginUser, UserNotFound }
import shop.services.Auth
import cats.syntax.all._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import shop.domain.tokenEncoder
import shop.ext.http4s.refined.RefinedRequestDecoder

final case class LoginRoutes[F[_]: JsonDecoder: MonadThrow](
    auth: Auth[F]
) extends Http4sDsl[F] {
  private val prefixRoutes = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case req @ POST -> Root / "login" =>
      req.decodeR[LoginUser] { user =>
        auth
          .login(user.username.toDomain, user.password.toDomain)
          .flatMap(Ok(_))
          .recoverWith {
            case UserNotFound(_) | InvalidPassword(_) => Forbidden()
          }
      }
  }
  val routes = Router(
    prefixRoutes -> httpRoutes
  )
}
