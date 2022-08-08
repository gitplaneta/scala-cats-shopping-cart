package shop.services

import cats.effect.{ MonadCancelThrow, Resource }
import shop.domain.brand.{ Brand, BrandId, BrandName }
import shop.domain.category.{ Category, CategoryId, CategoryName }
import shop.domain.item.{ CreateItem, ItemDescription, ItemId, ItemName }
import shop.effects.GenUUID
import skunk.Session
import squants.Money
import cats.syntax.all._
import shop.domain.ID
import skunk._
import skunk.implicits._

case class ItemCreation(
    brand: BrandName,
    category: CategoryName,
    name: ItemName,
    desc: ItemDescription,
    price: Money
)

trait TxItems[F[_]] {
  def create(item: ItemCreation): F[ItemId]
}

object TxItems {
  import BrandSQL._, CategoriesSQL._, ItemsSQL._

  def make[F[_]: GenUUID: MonadCancelThrow](
      postgres: Resource[F, Session[F]]
  ): TxItems[F] = new TxItems[F] {
    override def create(item: ItemCreation): F[ItemId] = postgres.use { s =>
      (s.prepare(insertBrand), s.prepare(insertCategory), s.prepare(insertItem)).tupled.use {
        case (ib, ic, it) =>
          s.transaction.surround(
            for {
              bid <- ID.make[F, BrandId]
              _   <- ib.execute(Brand(bid, item.brand)).void
              cid <- ID.make[F, CategoryId]
              _   <- ic.execute(Category(cid, item.category)).void
              tid <- ID.make[F, ItemId]
              itm = CreateItem(item.name, item.desc, item.price, bid, cid)
              _ <- it.execute(tid ~ itm).void
            } yield tid
          )
      }
    }
  }
}
