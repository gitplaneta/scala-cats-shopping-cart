package shop.modules

import cats.effect.Temporal
import org.typelevel.log4cats.Logger
import retry.{ RetryPolicies, RetryPolicy }
import shop.config.types.CheckoutConfig
import shop.effects.Background
import shop.programs.Checkout

sealed abstract class Programs[F[_]: Background: Logger: Temporal] private (
    cfg: CheckoutConfig,
    services: Services[F],
    clients: HttpClients[F]
) {
  val retryPolicy: RetryPolicy[F] = RetryPolicies.limitRetries(cfg.retriesLimit.value)

  val checkout: Checkout[F] = Checkout[F](
    clients.payment,
    services.cart,
    services.orders,
    retryPolicy
  )
}
