package shop.domain

import io.estatico.newtype.macros.newtype
import squants.market.Money

import java.util.UUID
import cart._
import derevo.cats.show
import derevo.derive

import scala.util.control.NoStackTrace

object order {
  @newtype case class OrderId(uuid: UUID)
  @newtype case class PaymentId(uuid: UUID)
  case class Order(
      id: OrderId,
      pid: PaymentId,
      items: Map[item.ItemId, Quantity],
      total: Money
  )

  @derive(show)
  case object EmptyCartError extends NoStackTrace
}
