package shop

import cats.effect._
import dev.profunktor.redis4cats.log4cats._
import cats.effect.std.Supervisor
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import shop.config.Config
import shop.modules._
import shop.programs.Programs
import shop.resources.{ AppResources, MkHttpServer }

object Main extends IOApp.Simple {

  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    Config.load[IO].flatMap { cfg =>
      Logger[IO].info(s"Loaded config $cfg") >>
        Supervisor[IO].use { implicit sp =>
          AppResources
            .make[IO](cfg)
            .evalMap { res =>
              Security.make[IO](cfg, res.postgres, res.redis).map { security =>
                val clients  = HttpClients.make[IO](cfg.paymentConfig, res.client)
                val services = Services.make[IO](res.redis, res.postgres, cfg.cartExpiration)
                val programs = Programs.make[IO](cfg.checkoutConfig, services, clients)
                val api      = HttpApi.make[IO](services, programs, security)
                cfg.httpServerConfig -> api.httpApp
              }
            }
            .flatMap {
              case (cfg, httpApp) =>
                MkHttpServer[IO].newEmber(cfg, httpApp)
            }
            .useForever
        }
    }

}
