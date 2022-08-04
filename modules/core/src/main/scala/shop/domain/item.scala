package shop.domain

import derevo.cats._
import derevo.circe.magnolia._
import derevo.derive
import io.estatico.newtype.macros.newtype
import shop.domain.brand._
import shop.domain.cart.CartItem
import shop.domain.cart.Quantity
import shop.domain.category._
import shop.optics.uuid
import squants.market._

import java.util.UUID

object item {
  @derive(decoder, encoder, keyDecoder, keyEncoder, eqv, show, uuid)
  @newtype
  case class ItemId(value: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype
  case class ItemName(value: String)

  @derive(decoder, encoder, eqv, show)
  @newtype
  case class ItemDescription(value: String)

  @derive(decoder, encoder, eqv, show)
  case class Item(
                   uuid: ItemId,
                   name: ItemName,
                   description: ItemDescription,
                   price: Money,
                   brand: Brand,
                   category: Category
                 ) {
    def cart(q: Quantity): CartItem =
      CartItem(this, q)
  }

  @derive(encoder, decoder)
  case class CreateItem(
      name: ItemName,
      description: ItemDescription,
      price: Money,
      brandId: brand.BrandId,
      categoryId: CategoryId
  )

  @derive(encoder, decoder)
  case class UpdateItem(
      id: ItemId,
      price: Money
  )

}
