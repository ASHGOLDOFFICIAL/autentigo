package org.aulune.authentigo.domain
package user

import scala.util.control.NoStackTrace


/** Errors that can occur during user validation. */
enum UserValidationError extends NoStackTrace:
  /** Email is invalid. */
  case InvalidEmail
