package shop.services

import cats.effect.kernel.{ Resource, Temporal }
import dev.profunktor.redis4cats.RedisCommands
import shop.domain.healthcheck.{ AppStatus, PostgresStatus, RedisStatus, Status }
import skunk._
import skunk.codec.all._
import skunk.implicits._
import cats.syntax.all._
import cats.effect._
import cats.effect.implicits._

import scala.concurrent.duration.DurationInt

trait HealthCheck[F[_]] {
  def status: F[AppStatus]

}

object HealthCheck {
  def make[F[_]: Temporal](postgres: Resource[F, Session[F]], redis: RedisCommands[F, String, String]): HealthCheck[F] =
    new HealthCheck[F] {

      val q: Query[Void, Int] = sql"select pid from pg_stat_activity".query(int4)

      val redisHealth: F[RedisStatus] = redis.ping
        .map(_.nonEmpty)
        .timeout(1.second)
        .map(Status._Bool.reverseGet)
        .orElse(Status.Unreachable.pure[F].widen)
        .map(RedisStatus.apply)

      val postgresHealth: F[PostgresStatus] =
        postgres
          .use(_.execute(q))
          .map(_.nonEmpty)
          .timeout(1.second)
          .map(Status._Bool.reverseGet)
          .orElse(Status.Unreachable.pure[F].widen)
          .map(PostgresStatus.apply)

      val status: F[AppStatus] = (redisHealth, postgresHealth).parMapN(AppStatus.apply)
    }
}
