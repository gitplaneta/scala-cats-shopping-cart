package shop.http.auth

import io.estatico.newtype.macros.newtype
import shop.domain.auth.UserId

object users {
  @newtype case class UserName(value: String)
  @newtype case class Password(value: String)
  @newtype case class EncryptedPassword(value: String)
  case class UserWithPassword(
      id: UserId,
      name: UserName,
      password: EncryptedPassword
  )
}
