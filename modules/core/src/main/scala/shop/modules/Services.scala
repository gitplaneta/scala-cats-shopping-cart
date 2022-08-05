package shop.modules

import shop.domain.brand.Brands
import shop.effects.GenUUID
import shop.services.{Categories, HealthCheck, Items, Orders, ShoppingCart}

sealed abstract class Services[F[_]] private(val cart: ShoppingCart[F],
                                             val brands: Brands[F],
                                             val categories: Categories[F],
                                             val items: Items[F],
                                             val orders: Orders[F],
                                             val healthCheck: HealthCheck[F]){

}

object Services {
//  def make[F[_]: GenUUID, Temporal]
}