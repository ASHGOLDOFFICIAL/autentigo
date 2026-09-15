package org.aulune.authentigo
package adapters
package user


import session.AccessTokenService

import application.user.{CreateUserRequest, UserInfo, UserService}
import domain.token.TokenString
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
 *  @tparam F effect type.
 */
final class UserServiceImpl[F[_]: MonadThrow: UUIDGen: LoggerFactory](
    repo: UserRepository[F],
    hasher: PasswordHasher[F],
    accessTokenService: AccessTokenService[F],
) extends UserService[F]:

  private given Logger[F] = LoggerFactory[F].getLogger

  override def createUser(
      request: CreateUserRequest,
  ): F[Either[ErrorResponse, UserInfo]] = (for
    _ <- eitherTLogger.info(s"User creation request: $request")
    id <- EitherT.right(UUIDGen[F].randomUUID.map(UserId.apply))
    user <- EitherT.fromEither[F](buildUser(id, request))
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

  /** Makes [[UserInfo]] out of a domain [[User]]. */
  private def toUserInfo(user: User): UserInfo =
    UserInfo(id = user.id, email = user.email)

  /** Builds a new user from a registration request.
   *  @param id ID to assign to the new user.
   *  @param request registration request.
   */
  private def buildUser(
      id: UserId,
      request: CreateUserRequest,
  ): Either[ErrorResponse, User] = Email(request.email)
    .toValidNec(UserValidationError.InvalidEmail)
    .andThen(email => User.create(id = id, email = email))
    .toEither
    .leftMap(UserServiceErrorResponses.invalidRegistrationDetails)

  /** Logs any error and returns internal error response. */
  private def handleInternal[A](e: Throwable) =
    for _ <- Logger[F].error(e)("Uncaught exception.")
    yield Internal.asLeft[A]
