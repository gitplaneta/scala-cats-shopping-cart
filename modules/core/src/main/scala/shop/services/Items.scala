package shop.services

import cats.effect.kernel.{ Concurrent, Resource }
import shop.domain.ID
import shop.domain.brand.BrandName
import shop.domain.item.{ CreateItem, Item, ItemId, UpdateItem }
import shop.effects.GenUUID
import shop.services.ItemsSQL._
import shop.sql.codecs._
import skunk._
import skunk.implicits._
import cats.syntax.all._

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def findBy(brand: BrandName): F[List[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[ItemId]
  def update(item: UpdateItem): F[Unit]
}

object Items {
  def make[F[_]: Concurrent: GenUUID](
      postgres: Resource[F, Session[F]]
  ): Items[F] = new Items[F] {
    override def findAll: F[List[Item]] = postgres.use(_.execute(selectAll))

    override def findBy(brand: BrandName): F[List[Item]] = postgres.use { s =>
      s.prepare(selectByBrand).use { ps =>
        ps.stream(brand, 1024).compile.toList
      }
    }

    override def findById(itemId: ItemId): F[Option[Item]] = postgres.use { s =>
      s.prepare(selectById).use { ps =>
        ps.option(itemId)
      }
    }

    override def create(item: CreateItem): F[ItemId] = postgres.use { s =>
      s.prepare(insertItem).use { ps =>
        ID.make[F, ItemId].flatMap { id =>
          ps.execute(id ~ item).as(id)
        }
      }
    }

    override def update(item: UpdateItem): F[Unit] = postgres.use { s =>
      s.prepare(updatedItem).use { ps =>
        ps.execute(item).void
      }
    }

    private def exerciseFindBy(brandName: BrandName): F[(List[Item], Boolean)] = postgres.use { s =>
      s.prepare(selectByBrand).use { ps =>
        ps.cursor(brandName)
          .use(c => {
            c.fetch(1024)
          })
      }
    }
  }
}

object ItemsSQL {
  val selectAll: Query[Void, Item] =
    sql"""
      SELECT i.uuid, i.name, i.description, i.price,
    b.uuid, b.name, c.uuid, c.name
    FROM items AS i
    INNER JOIN brands AS b ON i.brand_id = b.uuid
    INNER JOIN categories AS c ON i.category_id = c.uuid
       """.query(itemCodec)

  val selectById: Query[ItemId, Item] =
    sql"""
    SELECT i.uuid, i.name, i.description, i.price,
    b.uuid, b.name, c.uuid, c.name
    FROM items AS i
    INNER JOIN brands AS b ON i.brand_id = b.uuid
    INNER JOIN categories AS c ON i.category_id = c.uuid
    WHERE i.uuid = $itemId
       """.query(itemCodec)

  val selectByBrand: Query[BrandName, Item] =
    sql"""
  SELECT i.uuid, i.name, i.description, i.price,
  b.uuid, b.name, c.uuid, c.name
  FROM items AS i
  INNER JOIN brands AS b ON i.brand_id = b.uuid
  INNER JOIN categories AS c ON i.category_id = c.uuid
  WHERE b.name LIKE $brandName
  """.query(itemCodec)

  val insertItem: Command[ItemId ~ CreateItem] =
    sql"""
         INSERT INTO items
         VALUES (
           $itemId, $itemName, $itemDesc, $money, $brandId, $categoryId
         )
       """.command.contramap {
      case id ~ i => id ~ i.name ~ i.description ~ i.price ~ i.brandId ~ i.categoryId
    }

  val updatedItem: Command[UpdateItem] =
    sql"""
      update items
      SET price = $money
      WHERE uuid = $itemId
       """.command.contramap(c => c.price ~ c.id)
}
