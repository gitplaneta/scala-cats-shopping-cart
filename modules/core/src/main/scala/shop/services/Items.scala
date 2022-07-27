package shop.services

import shop.domain.brand.BrandName
import shop.domain.item.{Item, ItemId}

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def findBy(brand: BrandName): F[List[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: Item): F[Item]
  def update(item: Item): F[Item]

}
