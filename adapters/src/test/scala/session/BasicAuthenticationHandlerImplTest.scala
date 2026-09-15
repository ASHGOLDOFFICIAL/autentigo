package org.aulune.authentigo
package adapters
package session


import domain.user.{Email, User, UserId, UserRepository}

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.syntax.all.given
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.Assertion
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers


/** Tests for [[BasicAuthenticationHandlerImpl]]. */
final class BasicAuthenticationHandlerImplTest
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with AsyncMockFactory:

  private val mockRepo = mock[UserRepository[IO]]
  private val mockHasher = mock[PasswordHasher[IO]]

  private def stand(
      testCase: BasicAuthenticationHandler[IO] => IO[Assertion],
  ): IO[Assertion] = testCase(
    BasicAuthenticationHandlerImpl(
      repo = mockRepo,
      hasher = mockHasher,
    ))

  private val user = User.unsafe(
    id = UserId.unsafe("a18432b9-9552-4b95-8e8a-e36dba18c1ac"),
    email = Email.unsafe("user@example.com"),
    hashedPassword = Option("hash"),
  )
  private val password = "password"

  private def mockGetByEmail(returning: IO[Option[User]]) =
    (mockRepo.getByEmail _).expects(user.email).returning(returning)
  private def mockVerifyPassword(password: String, returning: IO[Boolean]) =
    (mockHasher.verifyPassword _)
      .expects(password, user.hashedPassword.get)
      .returning(returning)

  "authenticate method " - {
    "should " - {
      "return user if everything is OK" in stand { service =>
        val _ = mockGetByEmail(user.some.pure)
        val _ = mockVerifyPassword(password, true.pure)
        for result <- service.authenticate(user.email, password)
        yield result shouldBe user.some
      }

      "return None if user with given email doesn't exist" in stand { service =>
        val _ = mockGetByEmail(None.pure)
        for result <- service.authenticate(user.email, password)
        yield result shouldBe None
      }

      "return None if password is not valid" in stand { service =>
        val _ = mockGetByEmail(user.some.pure)
        val _ = mockVerifyPassword(password, false.pure)
        for result <- service.authenticate(user.email, password)
        yield result shouldBe None
      }
    }
  }
