package org.aulune.authentigo
package api
package user


import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.Decoder
import io.circe.Encoder
import application.user.ConfirmPasswordResetRequest
import application.user.CreateUserRequest
import application.user.RequestPasswordResetRequest
import application.user.UserInfo


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
