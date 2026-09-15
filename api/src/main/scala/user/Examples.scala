package org.aulune.authentigo
package api
package user


import org.aulune.authentigo.application.user.{CreateUserRequest, UserInfo}

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
