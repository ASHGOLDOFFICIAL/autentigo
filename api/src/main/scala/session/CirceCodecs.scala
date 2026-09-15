package org.aulune.authentigo
package api
package session


import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{
  deriveConfiguredDecoder,
  deriveConfiguredEncoder,
}
import io.circe.{Decoder, Encoder}
import org.aulune.authentigo.application.session.{CreateSessionRequest, Session}


/** [[Decoder]] and [[Encoder]] instances for [[SessionController]]. */
private[session] object CirceCodecs:
  private val basicAuthenticationName =
    classOf[CreateSessionRequest.BasicAuthentication].getSimpleName

  private given Configuration = baseConfig.copy(
    transformConstructorNames = {
      case `basicAuthenticationName` => "basic"
      case other                     => other
    },
  )

  given Encoder[CreateSessionRequest] = deriveConfiguredEncoder
  given Decoder[CreateSessionRequest] = deriveConfiguredDecoder

  given Encoder[Session] = deriveConfiguredEncoder
  given Decoder[Session] = deriveConfiguredDecoder
