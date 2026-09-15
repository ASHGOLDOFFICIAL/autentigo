package org.aulune.authentigo.application
package session

import org.aulune.commons.errors.ErrorReason


/** Errors that can occur while managing the `sessions` resource.
 *  @param reason machine-readable error name.
 */
enum SessionServiceError(val reason: String) extends ErrorReason(reason):
  /** Login credentials are invalid. */
  case InvalidCredentials extends SessionServiceError("INVALID_CREDENTIALS")
