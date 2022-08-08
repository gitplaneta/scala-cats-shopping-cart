package shop.services

import cats.data.NonEmptyList
import cats.effect.kernel.{ Concurrent, Resource }
import shop.domain.ID
import shop.domain.auth.UserId
import shop.domain.cart.{ CartItem, Quantity }
import shop.domain.item.ItemId
import shop.domain.order.{ Order, OrderId, PaymentId }
import shop.effects.GenUUID
import shop.sql.codecs._
import skunk._
import skunk.implicits._
import skunk.circe.codec.all._
import squants.market.Money
import cats.syntax.all._
trait Orders[F[_]] {
  def get(userId: UserId, orderId: OrderId): F[Option[Order]]

  def findBy(userId: UserId): F[List[Order]]

  def create(
      userId: UserId,
      paymentId: PaymentId,
      items: NonEmptyList[CartItem],
      total: Money
  ): F[OrderId]
}

object Orders {
  import OrdersSQL._

  def make[F[_]: Concurrent: GenUUID](postgres: Resource[F, Session[F]]): Orders[F] = new Orders[F] {
    override def get(userId: UserId, orderId: OrderId): F[Option[Order]] =
      postgres.use(s => s.prepare(selectByUserIdAndOrderId).use(ps => ps.option(userId ~ orderId)))

    override def findBy(userId: UserId): F[List[Order]] =
      postgres.use(
        _.prepare(selectByUserId).use(_.stream(userId, 1024).compile.toList)
      )

    override def create(userId: UserId, paymentId: PaymentId, items: NonEmptyList[CartItem], total: Money): F[OrderId] =
      postgres.use(_.prepare(insertOrder).use { cmd =>
        ID.make[F, OrderId].flatMap { oId =>
          val itMap = items.toList.map(x => x.item.uuid -> x.quantity).toMap
          val order = Order(oId, paymentId, itMap, total)
          cmd.execute(userId ~ order).as(oId)
        }
      })
  }
}

object OrdersSQL {
  val decoder: Decoder[Order] = (orderIdCodec ~ userIdCodec ~ paymentIdCodec ~ jsonb[Map[ItemId, Quantity]] ~ money)
    .map { case o ~ _ ~ p ~ i ~ t => Order(o, p, i, t) }
  val encoder: Encoder[UserId ~ Order] =
    (orderIdCodec ~ userIdCodec ~ paymentIdCodec ~ jsonb[Map[ItemId, Quantity]] ~ money)
      .contramap {
        case id ~ o => o.id ~ id ~ o.pid ~ o.items ~ o.total
      }

  val selectByUserId: Query[UserId, Order] =
    sql"""
      SELECT * FROM orders
      WHERE user_id = $userIdCodec
       """.query(decoder)

  val selectByUserIdAndOrderId: Query[UserId ~ OrderId, Order] =
    sql"""
      SELECT * FROM orders
      WHERE user_id = $userIdCodec AND uuid = $orderIdCodec
       """.query(decoder)

  val insertOrder: Command[UserId ~ Order] =
    sql"""
      INSERT INTO orders
      VALUES ($encoder)
       """.command
}
