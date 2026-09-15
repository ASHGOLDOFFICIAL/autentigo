package org.aulune.authentigo
package adapters
package session


import application.session.CreateSessionRequest.BasicAuthentication
import application.session.{CreateSessionRequest, Session, SessionService}
import domain.token.TokenString
import domain.user.{Email, User, UserRepository}

import cats.MonadThrow
import cats.data.EitherT
import cats.syntax.all.given
import org.aulune.commons.errors.ErrorResponse
import org.typelevel.log4cats.Logger.eitherTLogger
import org.typelevel.log4cats.syntax.given
import org.typelevel.log4cats.{Logger, LoggerFactory}


/** [[SessionService]] implementation.
 *  @param repo repository with users, used to look up the user a refresh token
 *    belongs to.
 *  @param basicAuthHandler service to which basic authentication requests will
 *    be delegated.
 *  @param accessTokenService service that generates access tokens.
 *  @param idTokenService service that generates ID tokens.
 *  @param refreshTokenService service that generates and decodes refresh
 *    tokens.
 *  @tparam F effect type.
 */
final class SessionServiceImpl[F[_]: MonadThrow: LoggerFactory](
    repo: UserRepository[F],
    basicAuthHandler: BasicAuthenticationHandler[F],
    accessTokenService: AccessTokenService[F],
    idTokenService: IdTokenService[F],
    refreshTokenService: RefreshTokenService[F],
) extends SessionService[F]:

  private given Logger[F] = LoggerFactory[F].getLogger

  override def createSession(
      request: CreateSessionRequest,
  ): F[Either[ErrorResponse, Session]] = (for
    _ <- eitherTLogger.info(s"Session creation request: $request")
    user <- delegateLogin(request)
    _ <- eitherTLogger.info(s"Successful login for $request")
    session <- EitherT.right(makeSessionForUser(user))
  yield session).value.handleErrorWith(handleInternal)

  override def refreshSession(
      refreshToken: String,
  ): F[Either[ErrorResponse, Session]] = (for
    token <- EitherT.fromOption(TokenString(refreshToken), Unauthenticated)
    userId <- EitherT.fromOptionF(
      refreshTokenService.decodeRefreshToken(token),
      Unauthenticated)
    user <- EitherT.fromOptionF(repo.get(userId), Unauthenticated)
    session <- EitherT.right(makeSessionForUser(user))
  yield session).value.handleErrorWith(handleInternal)

  /** Makes [[Session]] tokens for given user.
   *  @param user user for whom session is being made.
   */
  private def makeSessionForUser(user: User): F[Session] =
    for
      accessToken <- accessTokenService.generateAccessToken(user)
      idToken <- idTokenService.generateIdToken(user)
      refreshToken <- refreshTokenService.generateRefreshToken(user)
      _ <- info"Made session for user: $user."
    yield Session(
      accessToken = accessToken,
      idToken = idToken,
      refreshToken = refreshToken)

  /** Delegates login request to a service that can manage it.
   *  @param request login request.
   *  @return user if login is successful, otherwise `None`.
   */
  private def delegateLogin(
      request: CreateSessionRequest,
  ): EitherT[F, ErrorResponse, User] = request match
    case BasicAuthentication(email, password) =>
      for
        email <- EitherT
          .fromOption(
            Email(email),
            SessionServiceErrorResponses.InvalidCredentials)
          .leftSemiflatTap(_ => warn"Login with invalid email: $request.")
        user <- EitherT
          .fromOptionF(
            basicAuthHandler.authenticate(email, password),
            SessionServiceErrorResponses.InvalidCredentials)
          .leftSemiflatTap(_ => warn"Basic authentication failed: $request.")
      yield user

  /** Logs any error and returns internal error response. */
  private def handleInternal[A](e: Throwable) =
    for _ <- Logger[F].error(e)("Uncaught exception.")
    yield Internal.asLeft[A]
