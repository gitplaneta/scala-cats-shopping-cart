package shop.http.clients

import cats.effect.MonadCancelThrow
import cats.syntax.all._
import eu.timepit.refined.auto._
import org.http4s.Method._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl
import shop.config.types.PaymentConfig
import shop.domain.order._
import shop.domain.payment._

trait PaymentClient[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

object PaymentClient {
  def make[F[_]: JsonDecoder: MonadCancelThrow](
      cfg: PaymentConfig,
      client: Client[F]
  ): PaymentClient[F] = new PaymentClient[F] with Http4sClientDsl[F] {
    override def process(payment: Payment): F[PaymentId] = {
      Uri.fromString(cfg.uri.value + "/payments").liftTo[F].flatMap { uri =>
        client.run(POST(payment, uri)).use { resp =>
          resp.status match {
            case Status.Ok | Status.Conflict => resp.asJsonDecode
            case st => PaymentError(Option(st.reason).getOrElse("Unknown")).raiseError[F, PaymentId]
          }
        }
      }
    }
  }
}
