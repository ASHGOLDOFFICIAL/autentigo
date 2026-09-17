package org.aulune.authentigo
package adapters
package session


import cats.syntax.all.given
import application.session.SessionServiceError
import org.aulune.commons.errors.ErrorDetails
import org.aulune.commons.errors.ErrorInfo
import org.aulune.commons.errors.ErrorResponse
import org.aulune.commons.errors.ErrorStatus


/** Error responses for [[session.SessionServiceImpl]]. */
object SessionServiceErrorResponses:
  /** Given credentials are invalid. */
  val InvalidCredentials: ErrorResponse = ErrorResponse(
    status = ErrorStatus.Unauthenticated,
    message = "Login failed. Check your credentials.",
    details = ErrorDetails(
      info = ErrorInfo(
        reason = SessionServiceError.InvalidCredentials,
        domain = Domain,
      ).some,
    ),
  )
