package org.aulune.authentigo


import com.comcast.ip4s.{Host, Port}
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

  /** Config for authentication app.
   *
   *  @param issuer value to use in `iss` claims.
   *  @param key secret key to use for JWT tokens.
   *  @param accessExpiration time-to-live for access and ID tokens.
   *  @param refreshExpiration time-to-live for refresh tokens.
   */
  final case class Services(
      issuer: String,
      key: String,
      accessExpiration: FiniteDuration,
      refreshExpiration: FiniteDuration,
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
