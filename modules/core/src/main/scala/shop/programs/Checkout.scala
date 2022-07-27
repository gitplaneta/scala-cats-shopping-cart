package shop.programs

import cats.{Monad, MonadThrow}
import cats.data.NonEmptyList
import shop.domain.auth.UserId
import shop.domain.order.{EmptyCartError, OrderId}
import shop.domain.payment.Payment
import shop.http.clients.PaymentClient
import shop.services.{Orders, ShoppingCart}
import cats.implicits._

final case class Checkout[F[_]: Monad: MonadThrow](
    payments: PaymentClient[F],
    cart: ShoppingCart[F],
    orders: Orders[F]
) {

  private def ensureNonEmpty[A](xs: List[A]): F[NonEmptyList[A]] =
    MonadThrow[F].fromOption(
      NonEmptyList.fromList(xs),
      EmptyCartError
    )

  def process(userId: UserId, card: Card): F[OrderId] = for {
    c <- cart.get(userId)
    its <- ensureNonEmpty(c.items)
    pid <- payments.process(Payment(userId, c.total, card))
    oid <- orders.create(userId, pid, its, c.total)
    _ <- cart.delete(userId)
  } yield oid
}
