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
