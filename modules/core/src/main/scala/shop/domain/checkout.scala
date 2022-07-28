package shop.domain

import derevo.cats._
import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive

object checkout {
  @derive(decoder, encoder, show)
  case class Card(number: String, name: String, expiry: String, cvv: String)
}
