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
}
import domain.token.{TokenString, TotpSecret, VerificationCode}
import domain.user.{
  Email,
  User,
  UserConstraint,
  UserId,
  UserRepository,
  UserValidationError,
}

import cats.MonadThrow
import cats.data.EitherT
import cats.effect.std.UUIDGen
import cats.syntax.all.given
import org.aulune.commons.errors.ErrorResponse
import org.typelevel.log4cats.Logger.eitherTLogger
import org.typelevel.log4cats.syntax.given
import org.typelevel.log4cats.{Logger, LoggerFactory}


/** [[UserService]] implementation.
 *  @param repo repository with users.
 *  @param hasher password hasher.
 *  @param accessTokenService service used to decode the caller's access token
 *    for the self-only [[getUser]] check.
 *  @param codeService generates and verifies password-reset codes.
 *  @param emailSender sends password-reset codes to users.
 *  @tparam F effect type.
 */
final class UserServiceImpl[F[_]: MonadThrow: UUIDGen: LoggerFactory](
    repo: UserRepository[F],
    hasher: PasswordHasher[F],
    accessTokenService: AccessTokenService[F],
    codeService: VerificationCodeService[F],
    emailSender: EmailSender[F],
) extends UserService[F]:

  private given Logger[F] = LoggerFactory[F].getLogger

  override def createUser(
      request: CreateUserRequest,
  ): F[Either[ErrorResponse, UserInfo]] = (for
    _ <- eitherTLogger.info(s"User creation request: $request")
    id <- EitherT.right(UUIDGen[F].randomUUID.map(UserId.apply))
    totpSecret <- EitherT.right[ErrorResponse](codeService.generateSecret)
    user <- EitherT.fromEither[F](buildUser(id, totpSecret, request))
    hashed <- EitherT.right(hasher.hashPassword(request.password))
    withPassword <- EitherT.fromEither[F](
      user
        .update(hashedPassword = Some(hashed))
        .toEither
        .leftMap(UserServiceErrorResponses.invalidRegistrationDetails))
    persisted <- EitherT(repo.persist(withPassword).map {
      case Right(u)                         => u.asRight[ErrorResponse]
      case Left(UserConstraint.UniqueEmail) =>
        UserServiceErrorResponses.EmailTaken.asLeft
      case Left(UserConstraint.UniqueId) =>
        UserServiceErrorResponses.AlreadyRegistered.asLeft
    })
    _ <- eitherTLogger.info(s"Created user: $persisted.")
  yield toUserInfo(persisted)).value.handleErrorWith(handleInternal)

  override def getUser(
      id: String,
      accessToken: String,
  ): F[Either[ErrorResponse, UserInfo]] = (for
    _ <- eitherTLogger.info(s"Get user request for: $id.")
    userId <-
      EitherT.fromOption(UserId(id), UserServiceErrorResponses.InvalidUserId)
    token <- EitherT.fromOption(TokenString(accessToken), Unauthenticated)
    callerId <- EitherT.fromOptionF(
      accessTokenService.decodeAccessToken(token),
      Unauthenticated)
    _ <- EitherT.cond[F](
      callerId == userId,
      (),
      UserServiceErrorResponses.PermissionDenied)
    user <- EitherT.fromOptionF(
      repo.get(userId),
      UserServiceErrorResponses.UserNotFound)
  yield toUserInfo(user)).value.handleErrorWith(handleInternal)

  override def requestPasswordReset(
      request: RequestPasswordResetRequest,
  ): F[Either[ErrorResponse, Unit]] = (for
    _ <- eitherTLogger.info(s"Password reset requested for: ${request.email}")
    maybeUser <- EitherT.right[ErrorResponse](Email(request.email) match
      case Some(email) => repo.getByEmail(email)
      case None        => None.pure[F])
    _ <- EitherT.right[ErrorResponse](maybeUser match
      case Some(user) => issueAndSendCode(user)
      case None       => ().pure[F])
  yield ()).value.handleErrorWith(handleInternal)

  override def confirmPasswordReset(
      request: ConfirmPasswordResetRequest,
  ): F[Either[ErrorResponse, Unit]] = (for
    _ <-
      eitherTLogger.info(s"Password reset confirmation for: ${request.email}")
    email <- EitherT.fromOption(
      Email(request.email),
      UserServiceErrorResponses.InvalidPasswordReset)
    user <- EitherT.fromOptionF(
      repo.getByEmail(email),
      UserServiceErrorResponses.InvalidPasswordReset)
    code <- EitherT.fromOption(
      VerificationCode(request.code),
      UserServiceErrorResponses.InvalidPasswordReset)
    valid <- EitherT.right[ErrorResponse](
      codeService.verifyCode(user.totpSecret, code))
    _ <-
      EitherT.cond[F](valid, (), UserServiceErrorResponses.InvalidPasswordReset)
    newHash <-
      EitherT.right[ErrorResponse](hasher.hashPassword(request.newPassword))
    newSecret <- EitherT.right[ErrorResponse](codeService.generateSecret)
    updated <- EitherT.right[ErrorResponse](
      repo.updatePassword(
        user.id,
        newHash,
        newSecret,
        expectedTotpSecret = user.totpSecret))
    _ <- EitherT.cond[F](
      updated,
      (),
      UserServiceErrorResponses.InvalidPasswordReset)
    _ <- eitherTLogger.info(s"Password reset completed for user: ${user.id}")
  yield ()).value.handleErrorWith(handleInternal)

  /** Generates a reset code for `user` and emails it to them. */
  private def issueAndSendCode(user: User): F[Unit] =
    for
      code <- codeService.generateCode(user.totpSecret)
      _ <- emailSender.send(
        user.email,
        PasswordResetEmail.Subject,
        PasswordResetEmail.body(code))
    yield ()

  /** Makes [[UserInfo]] out of a domain [[User]]. */
  private def toUserInfo(user: User): UserInfo =
    UserInfo(id = user.id, email = user.email)

  /** Builds a new user from a registration request.
   *  @param id ID to assign to the new user.
   *  @param totpSecret secret to use for the new user's password-reset codes.
   *  @param request registration request.
   */
  private def buildUser(
      id: UserId,
      totpSecret: TotpSecret,
      request: CreateUserRequest,
  ): Either[ErrorResponse, User] = Email(request.email)
    .toValidNec(UserValidationError.InvalidEmail)
    .andThen(email =>
      User.create(id = id, email = email, totpSecret = totpSecret))
    .toEither
    .leftMap(UserServiceErrorResponses.invalidRegistrationDetails)

  /** Logs any error and returns internal error response. */
  private def handleInternal[A](e: Throwable) =
    for _ <- Logger[F].error(e)("Uncaught exception.")
    yield Internal.asLeft[A]
