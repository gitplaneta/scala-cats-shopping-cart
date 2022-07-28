package shop.domain

import squants.market.Money
import io.estatico.newtype.macros.newtype
import java.util.UUID
import shop.domain.brand.Brand
import shop.domain.category.{ Category, CategoryId }

object item {
  @newtype case class ItemId(value: UUID)
  @newtype case class ItemName(value: String)
  @newtype case class ItemDescription(value: String)

  case class Item(
      uuid: ItemId,
      name: ItemName,
      description: ItemDescription,
      price: Money,
      brand: Brand,
      category: Category
  )

  case class CreateItem(
      name: ItemName,
      description: ItemDescription,
      price: Money,
      brandId: brand.BrandId,
      categoryId: CategoryId
  )
  case class UpdateItem(
      id: ItemId,
      price: Money
  )

}
