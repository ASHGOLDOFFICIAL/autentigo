package org.aulune.authentigo
package adapters
package user


import session.AccessTokenService

import application.user.{
  ConfirmPasswordResetRequest,
  CreateUserRequest,
  RequestPasswordResetRequest,
  UserInfo,
  UserService,
  UserServiceError,
}
import domain.token.{TokenString, TotpSecret, VerificationCode}
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
  private val mockCodeService = mock[VerificationCodeService[IO]]
  private val mockEmailSender = mock[EmailSender[IO]]

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
      codeService = mockCodeService,
      emailSender = mockEmailSender,
    ))

  private val email = Email.unsafe("user@example.com")
  private val password = "password"
  private val hashed = "hashed_password"
  private val accessToken = TokenString.unsafe("access_token_string")
  private val totpSecret = TotpSecret.unsafe("totp_secret")
  private val newTotpSecret = TotpSecret.unsafe("new_totp_secret")
  private val resetCode = VerificationCode.unsafe("123456")

  private val user = User.unsafe(
    id = userId,
    email = email,
    hashedPassword = None,
    totpSecret = totpSecret,
  )
  private val persistedUser =
    user.update(hashedPassword = hashed.some).toOption.get
  private val userInfo = UserInfo(id = user.id, email = user.email)

  private val createUserRequest =
    CreateUserRequest(email = email, password = password)

  private def mockGenerateSecret(returning: IO[TotpSecret]) =
    (() => mockCodeService.generateSecret).expects().returning(returning)

  private def mockHashPassword(returning: IO[String]) =
    (mockHasher.hashPassword _).expects(password).returning(returning)

  private def mockPersist(returning: IO[Either[UserConstraint, User]]) =
    (mockRepo.persist _).expects(persistedUser).returning(returning)

  private def mockGet(returning: IO[Option[User]]) =
    (mockRepo.get _).expects(userId).returning(returning)

  private def mockGetByEmail(returning: IO[Option[User]]) =
    (mockRepo.getByEmail _).expects(email).returning(returning)

  private def mockDecodeAccessToken(returning: IO[Option[UserId]]) =
    (mockAccess.decodeAccessToken _).expects(accessToken).returning(returning)

  private def mockGenerateCode(returning: IO[VerificationCode]) =
    (mockCodeService.generateCode _).expects(totpSecret).returning(returning)

  private def mockSendEmail(returning: IO[Unit]) = (mockEmailSender.send _)
    .expects(
      user.email,
      PasswordResetEmail.Subject,
      PasswordResetEmail.body(resetCode))
    .returning(returning)

  private def mockVerifyCode(returning: IO[Boolean]) =
    (mockCodeService.verifyCode _)
      .expects(totpSecret, resetCode)
      .returning(returning)

  private def mockUpdatePassword(returning: IO[Boolean]) =
    (mockRepo.updatePassword _)
      .expects(userId, hashed, newTotpSecret, totpSecret)
      .returning(returning)

  "createUser method " - {
    "should " - {
      "return created user if everything is OK" in stand { service =>
        val _ = mockGenerateSecret(totpSecret.pure)
        val _ = mockHashPassword(hashed.pure)
        val _ = mockPersist(persistedUser.asRight.pure)
        for result <- service.createUser(createUserRequest)
        yield result shouldBe userInfo.asRight
      }

      "return InvalidUser if email is invalid" in stand { service =>
        val _ = mockGenerateSecret(totpSecret.pure)
        val request = createUserRequest.copy(email = "a")
        val result = service.createUser(request)
        assertDomainError(result)(UserServiceError.InvalidUser)
      }

      "return EmailAlreadyTaken if email is taken" in stand { service =>
        val _ = mockGenerateSecret(totpSecret.pure)
        val _ = mockHashPassword(hashed.pure)
        val _ = mockPersist(UserConstraint.UniqueEmail.asLeft.pure)
        val result = service.createUser(createUserRequest)
        assertDomainError(result)(UserServiceError.EmailAlreadyTaken)
      }

      "return UserAlreadyExists on ID collision" in stand { service =>
        val _ = mockGenerateSecret(totpSecret.pure)
        val _ = mockHashPassword(hashed.pure)
        val _ = mockPersist(UserConstraint.UniqueId.asLeft.pure)
        val result = service.createUser(createUserRequest)
        assertDomainError(result)(UserServiceError.UserAlreadyExists)
      }

      "handle exceptions from generateSecret gracefully" in stand { service =>
        val _ = mockGenerateSecret(IO.raiseError(new Throwable()))
        val result = service.createUser(createUserRequest)
        assertInternalError(result)
      }

      "handle exceptions from hashPassword gracefully" in stand { service =>
        val _ = mockGenerateSecret(totpSecret.pure)
        val _ = mockHashPassword(IO.raiseError(new Throwable()))
        val result = service.createUser(createUserRequest)
        assertInternalError(result)
      }

      "handle exceptions from persist gracefully" in stand { service =>
        val _ = mockGenerateSecret(totpSecret.pure)
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

  "requestPasswordReset method " - {
    "should " - {
      "email a code when the address is registered" in stand { service =>
        val _ = mockGetByEmail(user.some.pure)
        val _ = mockGenerateCode(resetCode.pure)
        val _ = mockSendEmail(().pure)
        for result <-
            service.requestPasswordReset(RequestPasswordResetRequest(email))
        yield result shouldBe ().asRight
      }

      "succeed without emailing anything for an unregistered address" in stand {
        service =>
          val _ = mockGetByEmail(None.pure)
          for result <-
              service.requestPasswordReset(RequestPasswordResetRequest(email))
          yield result shouldBe ().asRight
      }

      "succeed without a lookup for a malformed address" in stand { service =>
        for result <- service.requestPasswordReset(
            RequestPasswordResetRequest("not-an-email"))
        yield result shouldBe ().asRight
      }

      "handle exceptions from getByEmail gracefully" in stand { service =>
        val _ = mockGetByEmail(IO.raiseError(new Throwable()))
        val result =
          service.requestPasswordReset(RequestPasswordResetRequest(email))
        assertInternalError(result)
      }

      "handle exceptions from generateCode gracefully" in stand { service =>
        val _ = mockGetByEmail(user.some.pure)
        val _ = mockGenerateCode(IO.raiseError(new Throwable()))
        val result =
          service.requestPasswordReset(RequestPasswordResetRequest(email))
        assertInternalError(result)
      }

      "handle exceptions from emailSender.send gracefully" in stand { service =>
        val _ = mockGetByEmail(user.some.pure)
        val _ = mockGenerateCode(resetCode.pure)
        val _ = mockSendEmail(IO.raiseError(new Throwable()))
        val result =
          service.requestPasswordReset(RequestPasswordResetRequest(email))
        assertInternalError(result)
      }
    }
  }

  "confirmPasswordReset method " - {
    val request = ConfirmPasswordResetRequest(
      email = email,
      code = resetCode,
      newPassword = password,
    )

    "should " - {
      "reset the password when the code is valid" in stand { service =>
        val _ = mockGetByEmail(user.some.pure)
        val _ = mockVerifyCode(true.pure)
        val _ = mockHashPassword(hashed.pure)
        val _ = mockGenerateSecret(newTotpSecret.pure)
        val _ = mockUpdatePassword(true.pure)
        for result <- service.confirmPasswordReset(request)
        yield result shouldBe ().asRight
      }

      "return InvalidPasswordReset for an unregistered email" in stand {
        service =>
          val _ = mockGetByEmail(None.pure)
          val result = service.confirmPasswordReset(request)
          assertDomainError(result)(UserServiceError.InvalidPasswordReset)
      }

      "return InvalidPasswordReset for a malformed email" in stand { service =>
        val result =
          service.confirmPasswordReset(request.copy(email = "not-an-email"))
        assertDomainError(result)(UserServiceError.InvalidPasswordReset)
      }

      "return InvalidPasswordReset for a malformed code" in stand { service =>
        val _ = mockGetByEmail(user.some.pure)
        val result = service.confirmPasswordReset(request.copy(code = "abc"))
        assertDomainError(result)(UserServiceError.InvalidPasswordReset)
      }

      "return InvalidPasswordReset when the code doesn't verify" in stand {
        service =>
          val _ = mockGetByEmail(user.some.pure)
          val _ = mockVerifyCode(false.pure)
          val result = service.confirmPasswordReset(request)
          assertDomainError(result)(UserServiceError.InvalidPasswordReset)
      }

      "return InvalidPasswordReset when the update loses a race" in stand {
        service =>
          val _ = mockGetByEmail(user.some.pure)
          val _ = mockVerifyCode(true.pure)
          val _ = mockHashPassword(hashed.pure)
          val _ = mockGenerateSecret(newTotpSecret.pure)
          val _ = mockUpdatePassword(false.pure)
          val result = service.confirmPasswordReset(request)
          assertDomainError(result)(UserServiceError.InvalidPasswordReset)
      }

      "handle exceptions from verifyCode gracefully" in stand { service =>
        val _ = mockGetByEmail(user.some.pure)
        val _ = mockVerifyCode(IO.raiseError(new Throwable()))
        val result = service.confirmPasswordReset(request)
        assertInternalError(result)
      }

      "handle exceptions from updatePassword gracefully" in stand { service =>
        val _ = mockGetByEmail(user.some.pure)
        val _ = mockVerifyCode(true.pure)
        val _ = mockHashPassword(hashed.pure)
        val _ = mockGenerateSecret(newTotpSecret.pure)
        val _ = mockUpdatePassword(IO.raiseError(new Throwable()))
        val result = service.confirmPasswordReset(request)
        assertInternalError(result)
      }
    }
  }

end UserServiceImplTest
