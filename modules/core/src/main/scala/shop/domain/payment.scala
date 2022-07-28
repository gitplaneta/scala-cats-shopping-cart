package shop.domain

import shop.domain.auth._
import squants.market.Money

object payment {
  case class Payment(
      id: UserId,
      total: Money,
      card: checkout.Card
  )
}
