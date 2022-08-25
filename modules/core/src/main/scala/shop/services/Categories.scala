package shop.services

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import cats.syntax.all._
import shop.domain._
import shop.domain.category._
import shop.effects.GenUUID
import shop.sql.codecs._
import skunk._
import skunk.implicits._

trait Categories[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[CategoryId]

}

object Categories {
  def make[F[_]: GenUUID: MonadCancelThrow](
      postgres: Resource[F, Session[F]]
  ): Categories[F] = new Categories[F] {
    override def findAll: F[List[Category]] =
      postgres.use(_.execute(CategoriesSQL.selectAll))

    override def create(name: CategoryName): F[CategoryId] = postgres.use { session =>
      session.prepare(CategoriesSQL.insertCategory).use { cmd =>
        ID.make[F, CategoryId].flatMap(cId => cmd.execute(Category(cId, name)).as(cId))
      }
    }
  }
}

object CategoriesSQL {
  val selectAll: Query[Void, Category] =
    sql"""
      SELECT * FROM categories
       """.query(categoryCodec)
  val insertCategory: Command[Category] =
    sql"""
      INSERT INTO categories
      VALUES ($categoryCodec)
       """.command
}
