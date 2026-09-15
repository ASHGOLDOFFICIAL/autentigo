package org.aulune.authentigo
package api
package user


import org.aulune.authentigo.application.user.{CreateUserRequest, UserInfo}
import sttp.tapir.Schema


private[user] object TapirSchemas:
  given Schema[CreateUserRequest] = Schema.derived
  given Schema[UserInfo] = Schema.derived
