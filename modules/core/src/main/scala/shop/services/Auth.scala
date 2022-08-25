package shop.services

import cats.{ Applicative, Functor }
import shop.http.auth.users.{ AdminUser, CommonUser, Password, User, UserName }
import shop.domain.auth._
import dev.profunktor.auth.jwt.JwtToken
import dev.profunktor.redis4cats.RedisCommands
import pdi.jwt.JwtClaim
import io.circe.parser.decode
import io.circe.syntax._
import cats._
import cats.syntax.all._
import shop.auth.{ Crypto, Tokens }
import shop.config.types.TokenExpiration
import shop.domain._

trait Auth[F[_]] {
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]
}

object Auth {
  def make[F[_]: MonadThrow](
      tokenExpiration: TokenExpiration,
      tokens: Tokens[F],
      users: Users[F],
      redis: RedisCommands[F, String, String],
      crypto: Crypto
  ): Auth[F] = new Auth[F] {
    private val TokenExpiration = tokenExpiration.value

    override def newUser(username: UserName, password: Password): F[JwtToken] =
      users.find(username).flatMap {
        case Some(_) => MonadThrow[F].raiseError(UserNameInUse(username))
        case None =>
          for {
            i <- users.create(username, crypto.encrypt(password))
            t <- tokens.create
            u = User(i, username).asJson.noSpaces
            _ <- redis.setEx(t.value, u, TokenExpiration)
            _ <- redis.setEx(username.show, t.value, TokenExpiration)
          } yield t
      }

    override def login(username: UserName, password: Password): F[JwtToken] =
      users.find(username).flatMap {
        case None => UserNotFound(username).raiseError[F, JwtToken]
        case Some(user) if user.password =!= crypto.encrypt(password) =>
          InvalidPassword(user.name).raiseError[F, JwtToken]
        case Some(user) =>
          redis.get(username.show).flatMap {
            case Some(t) => JwtToken(t).pure[F]
            case None =>
              tokens.create.flatTap { t =>
                redis.setEx(t.value, user.asJson.noSpaces, TokenExpiration) *>
                  redis.setEx(username.show, t.value, TokenExpiration)
              }

          }
      }

    override def logout(token: JwtToken, username: UserName): F[Unit] =
      redis.del(token.show) *> redis.del(username.show).void
  }

}

trait UsersAuth[F[_], A] {
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}

object UsersAuth {
  def common[F[_]: Functor](redisCommands: RedisCommands[F, String, String]): UsersAuth[F, CommonUser] =
    new UsersAuth[F, CommonUser] {
      override def findUser(token: JwtToken)(claim: JwtClaim): F[Option[CommonUser]] =
        redisCommands
          .get(token.value)
          .map(_.flatMap(u => decode[User](u).toOption.map(CommonUser(_))))
    }

  def admin[F[_]: Applicative](adminToken: JwtToken, adminUser: AdminUser) = new UsersAuth[F, AdminUser] {
    override def findUser(token: JwtToken)(claim: JwtClaim): F[Option[AdminUser]] =
      (token === adminToken)
        .guard[Option]
        .as(adminUser)
        .pure[F]
  }
}
