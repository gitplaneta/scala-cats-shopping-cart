package shop.storage

import shop.domain._
import shop.domain.brand._
import shop.domain.category._
import shop.domain.item._
import shop.generators._
import shop.services._

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import natchez.Trace.Implicits.noop
import org.scalacheck.Gen
import skunk._
import skunk.implicits._

import shop.suite.ResourceSuite

object PostgresSuite extends ResourceSuite {
  type Res = Resource[IO, Session[IO]]

  override def sharedResource: Resource[IO, Resource[IO, Session[IO]]] =
    Session
      .pooled[IO](
        host = "localhost",
        port = 5432,
        user = "postgres",
        password = Some("my-password"),
        database = "store",
        max = 10
      )
}
