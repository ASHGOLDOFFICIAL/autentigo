package org.aulune.authentigo.application
package user

import org.aulune.commons.errors.ErrorResponse


/** Service managing the `users` resource.
 *  @tparam F effect type.
 */
trait UserService[F[_]]:
  /** Registers a new user.
   *
   *  [[UserServiceError.InvalidUser]] will be returned if registration details
   *  are invalid.
   *
   *  [[UserServiceError.EmailAlreadyTaken]] will be returned if the email is
   *  already taken.
   *
   *  [[UserServiceError.UserAlreadyExists]] will be returned on ID collision.
   *
   *  @param request registration details.
   */
  def createUser(
      request: CreateUserRequest,
  ): F[Either[ErrorResponse, UserInfo]]

  /** Returns a user by ID. Restricted to the user themselves.
   *
   *  Status `Unauthenticated` will be returned if the access token is missing
   *  or invalid.
   *
   *  Status `PermissionDenied` will be returned if the caller requests a user
   *  other than themselves.
   *
   *  [[UserServiceError.InvalidUserId]] will be returned if `id` isn't a valid
   *  resource ID.
   *
   *  [[UserServiceError.UserNotFound]] will be returned if no such user exists.
   *
   *  @param id ID of the user to fetch, as string.
   *  @param accessToken caller's access token, used to check that they're
   *    fetching their own resource.
   */
  def getUser(
      id: String,
      accessToken: String,
  ): F[Either[ErrorResponse, UserInfo]]

  /** Requests a password reset code be emailed to the given address.
   *
   *  To avoid leaking which emails are registered, this always succeeds with
   *  the same response whether or not the email belongs to a registered user.
   *
   *  @param request request containing the target email.
   */
  def requestPasswordReset(
      request: RequestPasswordResetRequest,
  ): F[Either[ErrorResponse, Unit]]

  /** Confirms a password reset using an emailed code and sets a new password.
   *
   *  [[UserServiceError.InvalidPasswordReset]] will be returned if the email
   *  isn't registered, or if the code is missing, doesn't match, or has
   *  expired.
   *
   *  @param request request with email, code, and new password.
   */
  def confirmPasswordReset(
      request: ConfirmPasswordResetRequest,
  ): F[Either[ErrorResponse, Unit]]
