package shop.optics

import java.util.UUID

import shop.ext.derevo.Derive

import monocle.Iso
import derevo.Derivation

trait IsUUID[A] {
  def _UUID: Iso[UUID, A]
}

object IsUUID {
  def apply[A: IsUUID]: IsUUID[A] = implicitly

  implicit val identityUUID: IsUUID[UUID] = new IsUUID[UUID] {
    val _UUID = Iso[UUID, UUID](identity)(identity)
  }
}

object uuid extends Derive[IsUUID]

object uuidCustom extends Derivation[IsUUID]
