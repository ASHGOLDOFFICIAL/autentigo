package org.aulune.authentigo
package api
package user


import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{
  deriveConfiguredDecoder,
  deriveConfiguredEncoder,
}
import io.circe.{Decoder, Encoder}
import application.user.{CreateUserRequest, UserInfo}


/** [[Decoder]] and [[Encoder]] instances for [[UserController]]. */
private[user] object CirceCodecs:
  private given Configuration = baseConfig

  given Encoder[CreateUserRequest] = deriveConfiguredEncoder
  given Decoder[CreateUserRequest] = deriveConfiguredDecoder

  given Encoder[UserInfo] = deriveConfiguredEncoder
  given Decoder[UserInfo] = deriveConfiguredDecoder
