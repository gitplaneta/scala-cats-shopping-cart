package shop.domain

import java.util.UUID
import io.estatico.newtype.macros.newtype

object brand {
  trait Brands[F[_]] {
    def findAll: F[List[Brand]]
    def create(name: BrandName): F[Brand]

  }

  @newtype case class BrandId(value: UUID)
  @newtype case class BrandName(value: String)

  case class Brand(uuid: BrandId, name: BrandName)
}
