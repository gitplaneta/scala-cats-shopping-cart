package shop.http.routes.auth

import cats.MonadThrow
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.domain.auth.{ CreateUser, UserNameInUse }
import shop.services.Auth
import cats.syntax.all._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import shop.domain.tokenEncoder
import shop.ext.http4s.refined.RefinedRequestDecoder

final case class UserRoutes[F[_]: JsonDecoder: MonadThrow](
    auth: Auth[F]
) extends Http4sDsl[F] {
  private val prefixPath = "/auth"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of {
    case req @ POST -> Root / "users" =>
      req.decodeR[CreateUser] { user =>
        auth
          .newUser(
            user.username.toDomain,
            user.password.toDomain
          )
          .flatMap(Created(_))
          .recoverWith {
            case UserNameInUse(u) => Conflict(u.show)
          }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
