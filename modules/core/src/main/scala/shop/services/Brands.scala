package shop.services

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import shop.sql.codecs._
import cats.syntax.all._
import shop.domain.ID
import shop.effects.GenUUID
import skunk._
import shop.domain.brand._
import shop.optics.IsUUID
import skunk.implicits._

trait Brands[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[BrandId]
}

object Brands {
  def make[F[_]: GenUUID: MonadCancelThrow](
      postgres: Resource[F, Session[F]]
  ): Brands[F] = {
    new Brands[F] {
      import BrandSQL._
      override def findAll: F[List[Brand]] = postgres.use(_.execute(selectAll))

      override def create(name: BrandName): F[BrandId] = postgres.use { session =>
        session.prepare(insertBrand).use { cmd =>
          ID.make[F, BrandId].flatMap { id =>
            cmd.execute(Brand(id, name)).as(id)
          }
        }
      }
    }
  }
}

object BrandSQL {
  val selectAll: Query[Void, Brand] = sql"""Select * from brands""".query(brandCodec)
  val insertBrand: Command[Brand] =
    sql"""
        INSERT INTO brands
        VALUES ($brandCodec)
         """.command

}
