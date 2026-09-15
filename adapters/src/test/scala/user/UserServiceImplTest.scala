package org.aulune.authentigo
package adapters
package user


import session.AccessTokenService

import application.user.{
  CreateUserRequest,
  UserInfo,
  UserService,
  UserServiceError,
}
import domain.token.TokenString
import domain.user.{Email, User, UserConstraint, UserId, UserRepository}

import cats.effect.IO
import cats.effect.std.UUIDGen
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

import java.util.UUID


/** Tests for [[UserServiceImpl]]. */
final class UserServiceImplTest
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with AsyncMockFactory:
  private given LoggerFactory[IO] = Slf4jFactory.create[IO]

  private val mockRepo = mock[UserRepository[IO]]
  private val mockHasher = mock[PasswordHasher[IO]]
  private val mockAccess = mock[AccessTokenService[IO]]

  private val uuid = UUID.fromString("00000000-0000-0000-0000-000000000001")
  private val userId = UserId(uuid)
  private given UUIDGen[IO] = new UUIDGen[IO]:
    override def randomUUID: IO[UUID] = uuid.pure[IO]

  private def stand(
      testCase: UserService[IO] => IO[Assertion],
  ): IO[Assertion] = testCase(
    UserServiceImpl(
      repo = mockRepo,
      hasher = mockHasher,
      accessTokenService = mockAccess,
    ))

  private val email = Email.unsafe("user@example.com")
  private val password = "password"
  private val hashed = "hashed_password"
  private val accessToken = TokenString.unsafe("access_token_string")

  private val user = User.unsafe(
    id = userId,
    email = email,
    hashedPassword = None,
  )
  private val persistedUser =
    user.update(hashedPassword = hashed.some).toOption.get
  private val userInfo = UserInfo(id = user.id, email = user.email)

  private val createUserRequest =
    CreateUserRequest(email = email, password = password)

  private def mockHashPassword(returning: IO[String]) =
    (mockHasher.hashPassword _).expects(password).returning(returning)

  private def mockPersist(returning: IO[Either[UserConstraint, User]]) =
    (mockRepo.persist _).expects(persistedUser).returning(returning)

  private def mockGet(returning: IO[Option[User]]) =
    (mockRepo.get _).expects(userId).returning(returning)

  private def mockDecodeAccessToken(returning: IO[Option[UserId]]) =
    (mockAccess.decodeAccessToken _).expects(accessToken).returning(returning)

  "createUser method " - {
    "should " - {
      "return created user if everything is OK" in stand { service =>
        val _ = mockHashPassword(hashed.pure)
        val _ = mockPersist(persistedUser.asRight.pure)
        for result <- service.createUser(createUserRequest)
        yield result shouldBe userInfo.asRight
      }

      "return InvalidUser if email is invalid" in stand { service =>
        val request = createUserRequest.copy(email = "a")
        val result = service.createUser(request)
        assertDomainError(result)(UserServiceError.InvalidUser)
      }

      "return EmailAlreadyTaken if email is taken" in stand { service =>
        val _ = mockHashPassword(hashed.pure)
        val _ = mockPersist(UserConstraint.UniqueEmail.asLeft.pure)
        val result = service.createUser(createUserRequest)
        assertDomainError(result)(UserServiceError.EmailAlreadyTaken)
      }

      "return UserAlreadyExists on ID collision" in stand { service =>
        val _ = mockHashPassword(hashed.pure)
        val _ = mockPersist(UserConstraint.UniqueId.asLeft.pure)
        val result = service.createUser(createUserRequest)
        assertDomainError(result)(UserServiceError.UserAlreadyExists)
      }

      "handle exceptions from hashPassword gracefully" in stand { service =>
        val _ = mockHashPassword(IO.raiseError(new Throwable()))
        val result = service.createUser(createUserRequest)
        assertInternalError(result)
      }

      "handle exceptions from persist gracefully" in stand { service =>
        val _ = mockHashPassword(hashed.pure)
        val _ = mockPersist(IO.raiseError(new Throwable()))
        val result = service.createUser(createUserRequest)
        assertInternalError(result)
      }
    }
  }

  "getUser method " - {
    "should " - {
      "return user info if everything is OK" in stand { service =>
        val _ = mockDecodeAccessToken(userId.some.pure)
        val _ = mockGet(user.some.pure)
        for result <- service.getUser(userId.toString, accessToken)
        yield result shouldBe userInfo.asRight
      }

      "return InvalidUserId if id isn't a valid UUID" in stand { service =>
        val result = service.getUser("not-a-uuid", accessToken)
        assertDomainError(result)(UserServiceError.InvalidUserId)
      }

      "result in Unauthenticated if token cannot be decoded" in stand {
        service =>
          val _ = mockDecodeAccessToken(None.pure)
          val result = service.getUser(userId.toString, accessToken)
          assertErrorStatus(result)(ErrorStatus.Unauthenticated)
      }

      "result in PermissionDenied if caller requests another user" in stand {
        service =>
          val otherId = UserId.unsafe("98d69db3-3975-4431-8979-4b39dc9c01f7")
          val _ = mockDecodeAccessToken(otherId.some.pure)
          val result = service.getUser(userId.toString, accessToken)
          assertErrorStatus(result)(ErrorStatus.PermissionDenied)
      }

      "result in UserNotFound if user cannot be found" in stand { service =>
        val _ = mockDecodeAccessToken(userId.some.pure)
        val _ = mockGet(None.pure)
        val result = service.getUser(userId.toString, accessToken)
        assertDomainError(result)(UserServiceError.UserNotFound)
      }

      "handle exceptions from decodeAccessToken gracefully" in stand {
        service =>
          val _ = mockDecodeAccessToken(IO.raiseError(new Throwable()))
          val result = service.getUser(userId.toString, accessToken)
          assertInternalError(result)
      }

      "handle exceptions from repo.get gracefully" in stand { service =>
        val _ = mockDecodeAccessToken(userId.some.pure)
        val _ = mockGet(IO.raiseError(new Throwable()))
        val result = service.getUser(userId.toString, accessToken)
        assertInternalError(result)
      }
    }
  }

end UserServiceImplTest
