package shop.modules

import shop.http.auth.users.{AdminJwtAuth, AdminUser, CommonUser, UserJwtAuth}
import shop.services.{Auth, UsersAuth}

sealed abstract class Security[F[_]] private (
    val auth: Auth[F],
    val adminAuth: UsersAuth[F, AdminUser],
    val usersAuth: UsersAuth[F, CommonUser],
    val adminJwtAuth: AdminJwtAuth,
    val userJwtAuth: UserJwtAuth
)
