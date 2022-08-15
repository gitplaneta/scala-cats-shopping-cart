package shop.domain

import monocle.law.discipline.IsoTests
import org.scalacheck.{Arbitrary, Cogen, Gen}
import shop.domain.brand.BrandId
import shop.domain.healthcheck.Status
import shop.generators
import shop.optics.IsUUID
import weaver._
import weaver.discipline.Discipline

import java.util.UUID

object OpticsSuite extends FunSuite with Discipline {

  implicit val arbStatus: Arbitrary[Status] = Arbitrary(Gen.oneOf(Status.Okay, Status.Unreachable))
  implicit val brandIdArb: Arbitrary[BrandId] = Arbitrary(generators.brandIdGen)
  implicit val brandIdCogen: Cogen[BrandId] = Cogen[UUID].contramap(_.value)

  checkAll("ISO[Status._Bool]", IsoTests(Status._Bool))

  checkAll("IsUUID[UUID]", IsoTests(IsUUID[UUID]._UUID))
  checkAll("IsUUID[BrandId]", IsoTests(IsUUID[BrandId]._UUID))

}
