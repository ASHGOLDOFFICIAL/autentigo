package org.aulune.authentigo


import adapters.Argon2iPasswordHasher
import adapters.SmtpEmailSender
import adapters.TotpVerificationCodeService
import adapters.session.BasicAuthenticationHandlerImpl
import adapters.session.JwtTokenService
import adapters.session.SessionServiceImpl
import adapters.user.PostgresUserRepository
import adapters.user.UserServiceImpl
import api.session.SessionController
import api.user.UserController
import migrations.Migrations

import cats.effect.kernel.Resource
import cats.effect.Async
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all.given
import doobie.Transactor
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.HttpRoutes
import org.http4s.server
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pureconfig.ConfigSource
import sttp.apispec.openapi.Server
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI


/** Main class. */
object App extends IOApp.Simple:
  private given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  private val config = ConfigSource.defaultReference.loadOrThrow[Config]
  private val transactor = Transactor.fromDriverManager[IO](
    driver = classOf[org.postgresql.Driver].getName,
    url = config.postgres.uri,
    user = config.postgres.user,
    password = config.postgres.password,
    logHandler = None,
  )

  override def run: IO[Unit] =
    for
      _ <- Migrations.run[IO](
        config.postgres.uri,
        config.postgres.user,
        config.postgres.password,
        "db/changelog/db.changelog-master.xml",
      )
      userRepo <- PostgresUserRepository.build[IO](transactor)
      hasher <- Argon2iPasswordHasher.build[IO]
      basicHandler = new BasicAuthenticationHandlerImpl[IO](userRepo, hasher)
      tokenServ = new JwtTokenService[IO](
        config.services.jwt.issuer,
        config.services.jwt.key,
        accessExpiration = config.services.jwt.accessExpiration,
        refreshExpiration = config.services.jwt.refreshExpiration,
      )
      codeService = new TotpVerificationCodeService[IO](
        config.services.passwordReset.expiration,
      )
      emailSender <- SmtpEmailSender.build[IO](
        host = config.services.smtp.host,
        port = config.services.smtp.port,
        username = config.services.smtp.username,
        password = config.services.smtp.password,
        fromAddress = config.services.smtp.fromAddress,
      )
      userService = new UserServiceImpl[IO](
        userRepo,
        hasher,
        tokenServ,
        codeService,
        emailSender,
      )
      sessionService = new SessionServiceImpl[IO](
        userRepo,
        basicHandler,
        tokenServ,
        tokenServ,
        tokenServ,
      )
      endpoints = new UserController[IO](userService).endpoints ++
        new SessionController[IO](sessionService).endpoints
      _ <- makeServer[IO](endpoints).use(_ => IO.never)
    yield ()

  private def makeServer[F[_]: Async: Network](
      endpoints: List[ServerEndpoint[Any, F]],
  ): Resource[F, server.Server] = EmberServerBuilder
    .default[F]
    .withHost(config.app.host)
    .withPort(config.app.port)
    .withHttpApp(makeRoutes(List("v1"), endpoints, config).orNotFound)
    .build

  private def makeRoutes[F[_]: Async](
      mountPoint: List[String],
      endpoints: List[ServerEndpoint[Any, F]],
      config: Config,
  ) =
    val appRoutes = Http4sServerInterpreter[F]().toRoutes(endpoints)
    val docsRoutes = makeSwaggerRoutes(mountPoint, endpoints, config)
    Router("/" + mountPoint.mkString("/") -> (appRoutes <+> docsRoutes))

  private def makeSwaggerRoutes[F[_]: Async](
      mountPoint: List[String],
      endpoints: List[ServerEndpoint[Any, F]],
      config: Config,
  ) = Http4sServerInterpreter[F]().toRoutes {
    val openApiYaml = OpenAPIDocsInterpreter()
      .toOpenAPI(
        endpoints.map(_.endpoint),
        title = config.app.name,
        version = config.app.version,
      )
      .addServer(
        Server(
          s"http://localhost:${config.app.port}/${mountPoint.mkString("/")}",
        )
          .description("Local development server"),
      )
      .toYaml
    SwaggerUI[F](openApiYaml)
  }
