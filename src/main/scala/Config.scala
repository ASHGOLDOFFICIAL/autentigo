package org.aulune.authentigo


import domain.user.Email

import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import pureconfig.ConfigReader
import pureconfig.error.ExceptionThrown

import scala.concurrent.duration.FiniteDuration


final case class Config(
    app: Config.App,
    postgres: Config.Postgres,
    services: Config.Services,
) derives ConfigReader


object Config:
  final case class App(
      name: String,
      version: String,
      host: Host,
      port: Port,
  )

  final case class Postgres(uri: String, user: String, password: String)

  final case class Services(
      jwt: Services.Jwt,
      passwordReset: Services.PasswordReset,
      smtp: Services.Smtp,
  )

  object Services:
    /** Config for JWT tokens.
     *  @param issuer value to use in `iss` claims.
     *  @param key secret key to use for JWT tokens.
     *  @param accessExpiration time-to-live for access and ID tokens.
     *  @param refreshExpiration time-to-live for refresh tokens.
     */
    final case class Jwt(
        issuer: String,
        key: String,
        accessExpiration: FiniteDuration,
        refreshExpiration: FiniteDuration,
    )

    /** Config for password-reset codes.
     *  @param expiration time-to-live for a password-reset code.
     */
    final case class PasswordReset(
        expiration: FiniteDuration,
    )

    /** Config for sending password-reset emails.
     *  @param host SMTP server host.
     *  @param port SMTP server port.
     *  @param username SMTP auth username.
     *  @param password SMTP auth password.
     *  @param fromAddress address emails are sent from.
     */
    final case class Smtp(
        host: Host,
        port: Port,
        username: String,
        password: String,
        fromAddress: Email,
    )

  given ConfigReader[Host] = ConfigReader.fromString { str =>
    Host.fromString(str) match
      case Some(h) => Right(h)
      case None    => Left(ExceptionThrown(new Exception("Incorrect host app")))
  }

  given ConfigReader[Port] = ConfigReader.fromString { str =>
    Port.fromString(str) match
      case Some(p) => Right(p)
      case None    => Left(ExceptionThrown(new Exception("Incorrect port app")))
  }

  given ConfigReader[Email] = ConfigReader.fromString { str =>
    Email(str) match
      case Some(e) => Right(e)
      case None    => Left(ExceptionThrown(new Exception("Incorrect email")))
  }
