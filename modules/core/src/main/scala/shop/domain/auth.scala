package shop.domain

import derevo.cats.{eqv, show}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import eu.timepit.refined.auto.autoUnwrap
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

import java.util.UUID
import shop.http.auth.users.{Password, UserName}
import shop.optics.uuid
import io.circe._
import io.circe.refined._

import javax.crypto.Cipher
import scala.util.control.NoStackTrace

object auth {
  @derive(decoder, encoder, eqv, show, uuid)
  @newtype
  case class UserId(value: UUID)

  @derive(decoder, encoder)
  @newtype
  case class UserNameParam(value: NonEmptyString) {
    def toDomain: UserName = UserName(value.toLowerCase)
  }

  @derive(decoder, encoder)
  @newtype
  case class PasswordParam(value: NonEmptyString) {
    def toDomain: Password = Password(value)
  }

  @newtype
  case class EncryptCipher(value: Cipher)

  @newtype
  case class DecryptCipher(value: Cipher)


  @derive(decoder, encoder)
  case class CreateUser(
                         username: UserNameParam,
                         password: PasswordParam
                       )

  case class UserNotFound(username: UserName) extends NoStackTrace

  case class UserNameInUse(username: UserName) extends NoStackTrace

  case class InvalidPassword(username: UserName) extends NoStackTrace

  case object UnsupportedOperation extends NoStackTrace

  case object TokenNotFound extends NoStackTrace
  @derive(decoder, encoder)
  case class LoginUser(
                        username: UserNameParam,
                        password: PasswordParam
                      )
}
