package org.aulune.authentigo
package adapters
package user


import cats.data.NonEmptyChain
import cats.syntax.all.given
import application.user.UserServiceError
import domain.user.UserValidationError
import org.aulune.commons.errors.ErrorDetails
import org.aulune.commons.errors.ErrorInfo
import org.aulune.commons.errors.ErrorResponse
import org.aulune.commons.errors.ErrorStatus


/** Error responses for [[user.UserServiceImpl]]. */
object UserServiceErrorResponses:
  /** The caller tried to access another user's resource. */
  val PermissionDenied: ErrorResponse = ErrorResponse(
    status = ErrorStatus.PermissionDenied,
    message = "You can only access your own user resource.",
    details = ErrorDetails(),
  )

  /** The given user ID isn't a valid resource ID (not a UUID). */
  val InvalidUserId: ErrorResponse = ErrorResponse(
    status = ErrorStatus.InvalidArgument,
    message = "Given user ID is not a valid UUID.",
    details = ErrorDetails(
      info =
        ErrorInfo(reason = UserServiceError.InvalidUserId, domain = Domain).some,
    ),
  )

  /** No user exists with the requested ID. */
  val UserNotFound: ErrorResponse = ErrorResponse(
    status = ErrorStatus.NotFound,
    message = "Account with given info doesn't exist. Maybe it was deleted.",
    details = ErrorDetails(
      info =
        ErrorInfo(reason = UserServiceError.UserNotFound, domain = Domain).some,
    ),
  )

  /** A user with the same ID already exists. */
  val AlreadyRegistered: ErrorResponse = ErrorResponse(
    status = ErrorStatus.AlreadyExists,
    message = "Account with given info already exists.",
    details = ErrorDetails(
      info = ErrorInfo(
        reason = UserServiceError.UserAlreadyExists,
        domain = Domain,
      ).some,
    ),
  )

  /** A user with the same email already exists. */
  val EmailTaken: ErrorResponse = ErrorResponse(
    status = ErrorStatus.AlreadyExists,
    message = "Account with given email already exists.",
    details = ErrorDetails(
      info = ErrorInfo(
        reason = UserServiceError.EmailAlreadyTaken,
        domain = Domain,
      ).some,
    ),
  )

  /** The email isn't registered, or the code is missing, incorrect, or has
   *  expired.
   */
  val InvalidPasswordReset: ErrorResponse = ErrorResponse(
    status = ErrorStatus.InvalidArgument,
    message = "Could not reset password with the given information.",
    details = ErrorDetails(
      info = ErrorInfo(
        reason = UserServiceError.InvalidPasswordReset,
        domain = Domain,
      ).some,
    ),
  )

  /** Makes one error response out of validation errors.
   *  @param errs user validation errors.
   */
  def invalidRegistrationDetails(
      errs: NonEmptyChain[UserValidationError],
  ): ErrorResponse = ErrorResponse(
    status = ErrorStatus.InvalidArgument,
    message = errs.map(validationErrorToString).mkString_(" "),
    details = ErrorDetails(
      info =
        ErrorInfo(reason = UserServiceError.InvalidUser, domain = Domain).some,
    ),
  )

  private def validationErrorToString(err: UserValidationError): String =
    err match
      case UserValidationError.InvalidEmail => "Email is invalid."
