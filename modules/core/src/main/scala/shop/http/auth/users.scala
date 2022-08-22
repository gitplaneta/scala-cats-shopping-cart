package shop.http.auth

import derevo.cats.{eqv, show}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import dev.profunktor.auth.jwt.JwtSymmetricAuth
import io.estatico.newtype.macros.newtype
import shop.domain.auth.UserId

object users {

  @newtype case class AdminJwtAuth(value: JwtSymmetricAuth)
  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)

  @derive(decoder, encoder, eqv, show)
  @newtype case class UserName(value: String)

  @derive(decoder, encoder, eqv, show)
  @newtype case class Password(value: String)

  @derive(decoder, encoder, eqv, show)
  @newtype
  @newtype case class EncryptedPassword(value: String)

  @derive(decoder, encoder, show)
  case class User(id: UserId, name: UserName)

  @derive(decoder, encoder)
  case class UserWithPassword(
      id: UserId,
      name: UserName,
      password: EncryptedPassword
  )

  @derive(show)
  @newtype
  case class CommonUser(value: User)

  @derive(show)
  @newtype
  case class AdminUser(value: User)
}
