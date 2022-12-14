package shop.http.routes

import cats.effect._
import org.http4s.Method._
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.syntax.literals._
import org.scalacheck.Gen
import shop.domain.ID
import shop.domain.brand._
import shop.generators._
import shop.services.Brands
import shop.suite.HttpSuite


object BrandRoutesSuite extends HttpSuite {


  def dataBrands(brands: List[Brand]) = new TestBrands {
    override def findAll: IO[List[Brand]] =
      IO.pure(brands)
  }

  def failingBrands(brands: List[Brand]) = new TestBrands {
    override def findAll: IO[List[Brand]] = IO.raiseError(DummyError) *> IO.pure(brands)
  }

  test("Get brands succeeds") {
    forall(Gen.listOf(brandGen)) { b =>
      val req = GET(uri"/brands")
      val routes = BrandRoutes[IO](dataBrands(b)).routes
      expectHttpBodyAndStatus(routes, req)(b, Status.Ok)
    }
  }

  test("GET brands fail") {
    forall(Gen.listOf(brandGen)) { b =>
      val req = GET(uri"/brands")
      val routes = BrandRoutes[IO](failingBrands(b)).routes
      expectHttpFailure(routes, req)
    }
  }


  protected class TestBrands extends Brands[IO] {
    def create(name: BrandName): IO[BrandId] = ID.make[IO, BrandId]

    def findAll: IO[List[Brand]] = IO.pure(List.empty)
  }


}
