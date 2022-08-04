package shop.domain

import io.estatico.newtype.macros.newtype
import squants.market.Money

import java.util.UUID
import cart._
import derevo.cats.{eqv, show}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import shop.optics.uuid

import scala.util.control.NoStackTrace

object order {
  @derive(uuid, encoder, decoder)
  @newtype case class OrderId(uuid: UUID)

  @derive(decoder, encoder, eqv, show, uuid)
  @newtype
  case class PaymentId(value: UUID)

  @derive(decoder, encoder)
  case class Order(
      id: OrderId,
      pid: PaymentId,
      items: Map[item.ItemId, Quantity],
      total: Money
  )

  @derive(show)
  case object EmptyCartError extends NoStackTrace


  @derive(show)
  sealed trait OrderOrPaymentError extends NoStackTrace {
    def cause: String
  }

  @derive(eqv, show)
  case class OrderError(cause: String)   extends OrderOrPaymentError
  case class PaymentError(cause: String) extends OrderOrPaymentError
}
