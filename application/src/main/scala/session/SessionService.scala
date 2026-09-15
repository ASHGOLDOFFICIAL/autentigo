package org.aulune.authentigo.application
package session

import org.aulune.commons.errors.ErrorResponse


/** Service managing the `sessions` resource.
 *  @tparam F effect type.
 */
trait SessionService[F[_]]:
  /** Creates a new session (logs in) if given credentials are correct.
   *
   *  [[SessionServiceError.InvalidCredentials]] will be returned if credentials
   *  are incorrect.
   *
   *  @param request request with authentication info.
   */
  def createSession(
      request: CreateSessionRequest,
  ): F[Either[ErrorResponse, Session]]

  /** Exchanges a refresh token for a new session.
   *
   *  An `Unauthenticated` status will be returned if the refresh token is
   *  missing, invalid, or expired.
   *
   *  @param refreshToken caller's refresh token.
   */
  def refreshSession(
      refreshToken: String,
  ): F[Either[ErrorResponse, Session]]
