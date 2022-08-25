package shop.modules

import cats.effect.MonadCancelThrow
import org.http4s.circe.JsonDecoder
import org.http4s.client.Client
import shop.config.types.PaymentConfig
import shop.http.clients.PaymentClient

object HttpClients {
  def make[F[_]: JsonDecoder: MonadCancelThrow](
      cfg: PaymentConfig,
      client: Client[F]
  ): HttpClients[F] = new HttpClients[F] {
    override def payment: PaymentClient[F] = PaymentClient.make(cfg, client)
  }
}

sealed trait HttpClients[F[_]] {
  def payment: PaymentClient[F]

}
