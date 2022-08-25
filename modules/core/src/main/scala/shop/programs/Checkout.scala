package shop.programs

import cats.MonadThrow
import cats.data.NonEmptyList
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import retry.RetryPolicy
import shop.domain.auth.UserId
import shop.domain.cart.CartItem
import shop.domain.checkout.Card
import shop.domain.order.EmptyCartError
import shop.domain.order.OrderError
import shop.domain.order.OrderId
import shop.domain.order.PaymentError
import shop.domain.order.PaymentId
import shop.domain.payment.Payment
import shop.effects.Background
import shop.http.clients.PaymentClient
import shop.http.retries.Retriable
import shop.http.retries.Retry
import shop.services.Orders
import shop.services.ShoppingCart
import squants.market.Money

import scala.concurrent.duration._

final case class Checkout[F[_]: Background: MonadThrow: Logger: Retry](
    payments: PaymentClient[F],
    cart: ShoppingCart[F],
    orders: Orders[F],
    policy: RetryPolicy[F]
) {

  private def ensureNonEmpty[A](xs: List[A]): F[NonEmptyList[A]] =
    MonadThrow[F].fromOption(
      NonEmptyList.fromList(xs),
      EmptyCartError
    )

  def process(userId: UserId, card: Card): F[OrderId] =
    for {
      c   <- cart.get(userId)
      its <- ensureNonEmpty(c.items)
      pid <- processPayment(Payment(userId, c.total, card))
      oid <- createOrder(userId, pid, its, c.total)
      _   <- cart.delete(userId).attempt.void
    } yield oid

  def processPayment(in: Payment): F[PaymentId] =
    Retry[F].retry(policy, Retriable.Payments)(payments.process(in)).adaptError {
      case e =>
        PaymentError(
          Option(e.getMessage).getOrElse("Unknown")
        )
    }

  def createOrder(
      userId: UserId,
      paymentId: PaymentId,
      items: NonEmptyList[CartItem],
      total: Money
  ): F[OrderId] = {
    val action = Retry[F]
      .retry(policy, Retriable.Orders)(
        orders.create(userId, paymentId, items, total)
      )
      .adaptError {
        case e => OrderError(e.getMessage)
      }

    def bgAction(fa: F[OrderId]): F[OrderId] = {
      fa.onError {
        case _ =>
          Logger[F].error(s"Failed to create order for: ${paymentId.show}") *> Background[F]
            .schedule(bgAction(fa), 1.hour)
      }
    }

    bgAction(action)
  }

}
