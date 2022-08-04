package shop.http

import cats.syntax.all._
import shop.domain.item.ItemId
import shop.domain.order.OrderId

import java.util.UUID

object vars {
  protected class UUIDVar[A](f: UUID => A) {
    def unapply(str: String): Option[A] = Either.catchNonFatal(f(UUID.fromString(str))).toOption
  }

  object ItemIdVar extends UUIDVar(ItemId.apply)
  object OrderIdVar extends UUIDVar(OrderId.apply)

}
