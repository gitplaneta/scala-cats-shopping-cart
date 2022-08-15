package shop.domain

import cats.kernel.laws.discipline.MonoidTests
import org.scalacheck.Arbitrary
import shop.generators
import squants.market.Money
import weaver._
import weaver.discipline.Discipline

object OrphanSuite extends FunSuite with Discipline {

  implicit val arbMoney: Arbitrary[Money] = Arbitrary[Money](generators.moneyGen)

  checkAll("Monoid[Money]", MonoidTests[Money].monoid)

}
