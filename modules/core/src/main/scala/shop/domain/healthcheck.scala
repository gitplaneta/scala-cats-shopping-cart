package shop.domain

import derevo.cats.eqv
import derevo.derive
import io.estatico.newtype.macros.newtype
import derevo.circe.magnolia.{ decoder, encoder }
import monocle.Iso
import io.circe.Encoder

object healthcheck {
  @derive(encoder)
  @newtype
  case class RedisStatus(value: Status)
  @derive(encoder)
  @newtype
  case class PostgresStatus(value: Status)

  @derive(encoder)
  case class AppStatus(
      redis: RedisStatus,
      postgres: PostgresStatus
  )

  @derive(eqv)
  sealed trait Status
  object Status {
    case object Okay        extends Status
    case object Unreachable extends Status

    val _Bool: Iso[Status, Boolean] = Iso[Status, Boolean] {
      case Okay        => true
      case Unreachable => false
    }(if (_) Okay else Unreachable)

    implicit val jsonEncoder: Encoder[Status] =
      Encoder.forProduct1("status")(_.toString)
  }
}
