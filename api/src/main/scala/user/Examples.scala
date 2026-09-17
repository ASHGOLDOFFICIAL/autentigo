package org.aulune.authentigo
package api
package user


import org.aulune.authentigo.application.user.{
  ConfirmPasswordResetRequest,
  CreateUserRequest,
  RequestPasswordResetRequest,
  UserInfo,
}

import java.util.UUID


private[user] object Examples:
  val CreateUserRequestExample: CreateUserRequest = CreateUserRequest(
    email = "user@example.com",
    password = "password",
  )

  val UserInfoExample: UserInfo = UserInfo(
    id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
    email = "user@example.com",
  )

  val RequestPasswordResetRequestExample: RequestPasswordResetRequest =
    RequestPasswordResetRequest(email = "user@example.com")

  val ConfirmPasswordResetRequestExample: ConfirmPasswordResetRequest =
    ConfirmPasswordResetRequest(
      email = "user@example.com",
      code = "123456",
      newPassword = "new-password",
    )
