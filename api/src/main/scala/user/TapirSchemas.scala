package org.aulune.authentigo
package api
package user


import org.aulune.authentigo.application.user.{
  ConfirmPasswordResetRequest,
  CreateUserRequest,
  RequestPasswordResetRequest,
  UserInfo,
}
import sttp.tapir.Schema


private[user] object TapirSchemas:
  given Schema[CreateUserRequest] = Schema.derived
  given Schema[UserInfo] = Schema.derived
  given Schema[RequestPasswordResetRequest] = Schema.derived
  given Schema[ConfirmPasswordResetRequest] = Schema.derived
