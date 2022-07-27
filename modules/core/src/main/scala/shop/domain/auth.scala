package shop.domain

import io.estatico.newtype.macros.newtype
import java.util.UUID
import shop.http.auth.users

object auth {
  @newtype case class UserId(value: UUID)
  @newtype case class JwtToken(value: String)
  case class User(id: UserId, name: users.UserName)
}
