package org.aulune.authentigo.application
package user

import org.aulune.commons.errors.ErrorReason


/** Errors that can occur while managing the `users` resource.
 *  @param reason machine-readable error name.
 */
enum UserServiceError(val reason: String) extends ErrorReason(reason):
  /** Given user ID isn't a valid resource ID. */
  case InvalidUserId extends UserServiceError("INVALID_USER_ID")

  /** Registration details don't satisfy requirements. */
  case InvalidUser extends UserServiceError("INVALID_USER")

  /** Requested user doesn't exist. */
  case UserNotFound extends UserServiceError("USER_NOT_FOUND")

  /** User already exists. */
  case UserAlreadyExists extends UserServiceError("USER_ALREADY_EXISTS")

  /** Email already taken. */
  case EmailAlreadyTaken extends UserServiceError("EMAIL_ALREADY_TAKEN")
