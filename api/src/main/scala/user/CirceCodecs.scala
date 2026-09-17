package org.aulune.authentigo
package api
package user


import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{
  deriveConfiguredDecoder,
  deriveConfiguredEncoder,
}
import io.circe.{Decoder, Encoder}
import application.user.{
  ConfirmPasswordResetRequest,
  CreateUserRequest,
  RequestPasswordResetRequest,
  UserInfo,
}


/** [[Decoder]] and [[Encoder]] instances for [[UserController]]. */
private[user] object CirceCodecs:
  private given Configuration = baseConfig

  given Encoder[CreateUserRequest] = deriveConfiguredEncoder
  given Decoder[CreateUserRequest] = deriveConfiguredDecoder

  given Encoder[UserInfo] = deriveConfiguredEncoder
  given Decoder[UserInfo] = deriveConfiguredDecoder

  given Encoder[RequestPasswordResetRequest] = deriveConfiguredEncoder
  given Decoder[RequestPasswordResetRequest] = deriveConfiguredDecoder

  given Encoder[ConfirmPasswordResetRequest] = deriveConfiguredEncoder
  given Decoder[ConfirmPasswordResetRequest] = deriveConfiguredDecoder
