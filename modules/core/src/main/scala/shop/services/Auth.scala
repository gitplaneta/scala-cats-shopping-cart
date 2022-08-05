package shop.services

import shop.http.auth.users.UserName
import shop.domain.auth._
import shop.http.auth.users.Password
import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.JwtClaim

trait Auth[F[_]] {
  def findUser(token: JwtToken): F[Option[User]]
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]
}

trait UsersAuth[F[_], A] {
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}
