package org.aulune.authentigo
package adapters
package session


import application.session.{
  CreateSessionRequest,
  Session,
  SessionService,
  SessionServiceError,
}
import domain.token.TokenString
import domain.user.{Email, User, UserId, UserRepository}

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all.given
import org.aulune.commons.errors.ErrorStatus
import org.aulune.commons.errors.ErrorAssertions.{
  assertDomainError,
  assertErrorStatus,
  assertInternalError,
}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.Assertion
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory


/** Tests for [[SessionServiceImpl]]. */
final class SessionServiceImplTest
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with AsyncMockFactory:
  private given LoggerFactory[IO] = Slf4jFactory.create[IO]

  private val mockRepo = mock[UserRepository[IO]]
  private val mockBasic = mock[BasicAuthenticationHandler[IO]]
  private val mockAccess = mock[AccessTokenService[IO]]
  private val mockId = mock[IdTokenService[IO]]
  private val mockRefresh = mock[RefreshTokenService[IO]]

  private def stand(
      testCase: SessionService[IO] => IO[Assertion],
  ): IO[Assertion] = testCase(
    SessionServiceImpl(
      repo = mockRepo,
      basicAuthHandler = mockBasic,
      accessTokenService = mockAccess,
      idTokenService = mockId,
      refreshTokenService = mockRefresh,
    ))

  private val userId = UserId.unsafe("00000000-0000-0000-0000-000000000001")
  private val email = Email.unsafe("user@example.com")
  private val password = "password"
  private val accessToken = TokenString.unsafe("access_token_string")
  private val idToken = TokenString.unsafe("id_token_string")
  private val refreshToken = TokenString.unsafe("refresh_token_string")
  private val newRefreshToken = TokenString.unsafe("new_refresh_token_string")

  private val user = User.unsafe(
    id = userId,
    email = email,
    hashedPassword = None,
  )
  private val session = Session(
    accessToken = accessToken,
    idToken = idToken,
    refreshToken = refreshToken,
  )

  private val basicRequest = CreateSessionRequest.BasicAuthentication(
    email = email,
    password = password,
  )

  private def mockAuthenticate(returning: IO[Option[User]]) =
    (mockBasic.authenticate _)
      .expects(user.email, password)
      .returning(returning)

  private def mockGenerateAccessToken(returning: IO[TokenString]) =
    (mockAccess.generateAccessToken _).expects(user).returning(returning)

  private def mockGenerateIdToken(returning: IO[TokenString]) =
    (mockId.generateIdToken _).expects(user).returning(returning)

  private def mockGenerateRefreshToken(returning: IO[TokenString]) =
    (mockRefresh.generateRefreshToken _).expects(user).returning(returning)

  private def mockDecodeRefreshToken(returning: IO[Option[UserId]]) =
    (mockRefresh.decodeRefreshToken _)
      .expects(refreshToken)
      .returning(returning)

  private def mockGet(returning: IO[Option[User]]) =
    (mockRepo.get _).expects(userId).returning(returning)

  "createSession method with basic authentication " - {
    "should " - {
      "return tokens for user if everything is OK" in stand { service =>
        val _ = mockAuthenticate(user.some.pure)
        val _ = mockGenerateAccessToken(accessToken.pure)
        val _ = mockGenerateIdToken(idToken.pure)
        val _ = mockGenerateRefreshToken(refreshToken.pure)
        for result <- service.createSession(basicRequest)
        yield result shouldBe session.asRight
      }

      "return InvalidCredentials if couldn't authenticate" in stand { service =>
        val _ = mockAuthenticate(None.pure)
        val result = service.createSession(basicRequest)
        assertDomainError(result)(SessionServiceError.InvalidCredentials)
      }

      "handle exceptions from authenticate gracefully" in stand { service =>
        val _ = mockAuthenticate(IO.raiseError(new Throwable()))
        val result = service.createSession(basicRequest)
        assertInternalError(result)
      }

      "handle exceptions from generateAccessToken gracefully" in stand {
        service =>
          val _ = mockAuthenticate(user.some.pure)
          val _ = mockGenerateAccessToken(IO.raiseError(new Throwable()))
          val result = service.createSession(basicRequest)
          assertInternalError(result)
      }

      "handle exceptions from generateIdToken gracefully" in stand { service =>
        val _ = mockAuthenticate(user.some.pure)
        val _ = mockGenerateAccessToken(accessToken.pure)
        val _ = mockGenerateIdToken(IO.raiseError(new Throwable()))
        val result = service.createSession(basicRequest)
        assertInternalError(result)
      }

      "handle exceptions from generateRefreshToken gracefully" in stand {
        service =>
          val _ = mockAuthenticate(user.some.pure)
          val _ = mockGenerateAccessToken(accessToken.pure)
          val _ = mockGenerateIdToken(idToken.pure)
          val _ = mockGenerateRefreshToken(IO.raiseError(new Throwable()))
          val result = service.createSession(basicRequest)
          assertInternalError(result)
      }
    }
  }

  "refreshSession method " - {
    "should " - {
      "return new tokens if everything is OK" in stand { service =>
        val _ = mockDecodeRefreshToken(userId.some.pure)
        val _ = mockGet(user.some.pure)
        val _ = mockGenerateAccessToken(accessToken.pure)
        val _ = mockGenerateIdToken(idToken.pure)
        val _ = mockGenerateRefreshToken(newRefreshToken.pure)
        for result <- service.refreshSession(refreshToken)
        yield result shouldBe session
          .copy(refreshToken = newRefreshToken)
          .asRight
      }

      "result in Unauthenticated if token cannot be decoded" in stand {
        service =>
          val _ = mockDecodeRefreshToken(None.pure)
          val result = service.refreshSession(refreshToken)
          assertErrorStatus(result)(ErrorStatus.Unauthenticated)
      }

      "result in Unauthenticated if token owner cannot be found" in stand {
        service =>
          val _ = mockDecodeRefreshToken(userId.some.pure)
          val _ = mockGet(None.pure)
          val result = service.refreshSession(refreshToken)
          assertErrorStatus(result)(ErrorStatus.Unauthenticated)
      }

      "handle exceptions from decodeRefreshToken gracefully" in stand {
        service =>
          val _ = mockDecodeRefreshToken(IO.raiseError(new Throwable()))
          val result = service.refreshSession(refreshToken)
          assertInternalError(result)
      }

      "handle exceptions from repo.get gracefully" in stand { service =>
        val _ = mockDecodeRefreshToken(userId.some.pure)
        val _ = mockGet(IO.raiseError(new Throwable()))
        val result = service.refreshSession(refreshToken)
        assertInternalError(result)
      }
    }
  }

end SessionServiceImplTest
