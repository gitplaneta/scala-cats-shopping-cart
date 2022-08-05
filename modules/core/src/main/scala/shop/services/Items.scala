package shop.services

import shop.domain.brand.BrandName
import shop.domain.item.{CreateItem, Item, ItemId, UpdateItem}

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def findBy(brand: BrandName): F[List[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[Item]
  def update(item: UpdateItem): F[Item]

}
