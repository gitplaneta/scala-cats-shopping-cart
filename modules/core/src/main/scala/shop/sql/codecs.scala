package shop.sql

import shop.domain.auth.UserId
import shop.domain.brand.{Brand, BrandId, BrandName}
import shop.domain.category.{Category, CategoryId, CategoryName}
import shop.domain.item._
import shop.domain.order.{OrderId, PaymentId}
import shop.http.auth.users.{EncryptedPassword, UserName}
import skunk.Codec
import skunk._
import skunk.codec.all._
import skunk.implicits._
import squants.market.{Money, USD}

object codecs {

  val brandId: Codec[BrandId]     = uuid.imap[BrandId](BrandId(_))(_.value)
  val brandName: Codec[BrandName] = varchar.imap[BrandName](BrandName(_))(_.value)
  val brandCodec: Codec[Brand] = (brandId ~ brandName).imap {
    case i ~ n => Brand(i, n)
  }(b => (b.uuid ~ b.name))

  val categoryId: Codec[CategoryId]     = uuid.imap[CategoryId](CategoryId(_))(_.value)
  val categoryName: Codec[CategoryName] = varchar.imap[CategoryName](CategoryName(_))(_.value)
  val categoryCodec: Codec[Category] = (categoryId ~ categoryName).imap {
    case i ~ n => Category(i, n)
  }(c => c.uuid ~ c.name)

  val money: Codec[Money] = numeric.imap[Money](USD(_))(_.amount)

  val itemId: Codec[ItemId]            = uuid.imap[ItemId](ItemId(_))(_.value)
  val itemName: Codec[ItemName]        = varchar.imap[ItemName](ItemName(_))(_.value)
  val itemDesc: Codec[ItemDescription] = varchar.imap[ItemDescription](ItemDescription(_))(_.value)
  val itemCodec: Decoder[Item] = (itemId ~ itemName ~ itemDesc ~ money ~ brandId ~
    brandName ~ categoryId ~ categoryName).map {
    case i ~ n ~ d ~ p ~ bi ~ bn ~ ci ~ cn =>
      Item(i, n, d, p, Brand(bi, bn), Category(ci, cn))
  }

  val orderIdCodec: Codec[OrderId] = uuid.imap[OrderId](OrderId(_))(_.uuid)
  val paymentIdCodec: Codec[PaymentId] = uuid.imap[PaymentId](PaymentId(_))(_.value)

  val userIdCodec: Codec[UserId] = uuid.imap[UserId](UserId(_))(_.value)
  val userNameCodec: Codec[UserName] = varchar.imap[UserName](UserName(_))(_.value)

  val encPassword: Codec[EncryptedPassword] = varchar.imap[EncryptedPassword](EncryptedPassword(_))(_.value)

}
