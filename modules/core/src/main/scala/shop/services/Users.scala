package shop.services

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import shop.domain.auth.{UserId, UserNameInUse}
import shop.effects.GenUUID
import shop.http.auth.users.{EncryptedPassword, User, UserName, UserWithPassword}
import shop.sql.codecs._
import skunk.Codec
import skunk._
import skunk.implicits._
import cats.syntax.all._
import shop.domain.ID
trait Users[F[_]] {
  def find(
      username: UserName
  ): F[Option[UserWithPassword]]
  def create(
      username: UserName,
      password: EncryptedPassword
  ): F[UserId]
}

object Users {
  import UserSQL._
  def make[F[_]: GenUUID: MonadCancelThrow](postgres: Resource[F, Session[F]]) = new Users[F] {
    override def find(username: UserName): F[Option[UserWithPassword]] =
      postgres.use(
        s =>
          s.prepare(selectUser).use { q =>
            q.option(username).map {
              case Some(u ~ p) => UserWithPassword(u.id, u.name, p).some
              case _           => none[UserWithPassword]
            }
          }
      )

    override def create(username: UserName, password: EncryptedPassword): F[UserId] = postgres.use(s => s.prepare(insertUser).use {pc =>
      ID.make[F, UserId]
        .flatMap( uId => pc.execute(User(uId, username) ~ password).as(uId))
        .recoverWith {
          case SqlState.UniqueViolation(_) => UserNameInUse(username).raiseError[F, UserId]
        }
    })
  }
}

object UserSQL {
  val codec: Codec[User ~ EncryptedPassword] = (userIdCodec ~ userNameCodec ~ encPassword).imap {
    case i ~ n ~ p => User(i, n) ~ p
  } {
    case u ~ p => u.id ~ u.name ~ p
  }

  val selectUser: Query[UserName, User ~ EncryptedPassword] =
    sql"""
      SELECT * FROM users
      where name = $userNameCodec
       """.query(codec)

  val insertUser: Command[User ~ EncryptedPassword] =
    sql"""
      insert into users
      values ($codec)
       """.command
}
