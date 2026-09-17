package org.aulune.authentigo
package api
package user


import org.aulune.authentigo.application.user.ConfirmPasswordResetRequest
import org.aulune.authentigo.application.user.CreateUserRequest
import org.aulune.authentigo.application.user.RequestPasswordResetRequest
import org.aulune.authentigo.application.user.UserInfo
import sttp.tapir.Schema


private[user] object TapirSchemas:
  given Schema[CreateUserRequest] = Schema.derived
  given Schema[UserInfo] = Schema.derived
  given Schema[RequestPasswordResetRequest] = Schema.derived
  given Schema[ConfirmPasswordResetRequest] = Schema.derived
