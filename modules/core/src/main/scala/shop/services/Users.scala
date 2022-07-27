package shop.services

import shop.domain.auth.UserId
import shop.http.auth.users.{EncryptedPassword, UserName, UserWithPassword}

trait Users[F[_]] {
  def find(
      username: UserName
  ): F[Option[UserWithPassword]]
  def create(
      username: UserName,
      password: EncryptedPassword
  ): F[UserId]
}
