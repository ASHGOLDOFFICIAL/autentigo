package org.aulune.commons
package errors
package adapters.tapir


import errors.ErrorStatus.Aborted
import errors.ErrorStatus.AlreadyExists
import errors.ErrorStatus.Cancelled
import errors.ErrorStatus.DataLoss
import errors.ErrorStatus.DeadlineExceeded
import errors.ErrorStatus.FailedPrecondition
import errors.ErrorStatus.Internal
import errors.ErrorStatus.InvalidArgument
import errors.ErrorStatus.NotFound
import errors.ErrorStatus.OutOfRange
import errors.ErrorStatus.PermissionDenied
import errors.ErrorStatus.ResourceExhausted
import errors.ErrorStatus.Unauthenticated
import errors.ErrorStatus.Unavailable
import errors.ErrorStatus.Unimplemented
import errors.ErrorStatus.Unknown
import errors.ErrorResponse
import errors.ErrorStatus

import sttp.model.StatusCode


/** Mapper between [[ErrorStatus]] and [[StatusCode]]. */
object ErrorStatusCodeMapper:
  /** Converts [[ErrorStatus]] to corresponding [[StatusCode]].
   *
   *  @param err application error.
   *  @return corresponding status code.
   */
  private def toStatusCode(err: ErrorStatus): StatusCode = err match
    case Cancelled          => StatusCode(499)
    case Unknown            => StatusCode.InternalServerError
    case InvalidArgument    => StatusCode.BadRequest
    case DeadlineExceeded   => StatusCode.GatewayTimeout
    case NotFound           => StatusCode.NotFound
    case AlreadyExists      => StatusCode.Conflict
    case PermissionDenied   => StatusCode.Forbidden
    case Unauthenticated    => StatusCode.Unauthorized
    case ResourceExhausted  => StatusCode.TooManyRequests
    case FailedPrecondition => StatusCode.BadRequest
    case Aborted            => StatusCode.Conflict
    case OutOfRange         => StatusCode.BadRequest
    case Unimplemented      => StatusCode.NotImplemented
    case Internal           => StatusCode.InternalServerError
    case Unavailable        => StatusCode.ServiceUnavailable
    case DataLoss           => StatusCode.InternalServerError

  /** Returns response itself with corresponding error code.
   *  @param response error response from application layer.
   */
  def toApiResponse(
      response: ErrorResponse,
  ): (StatusCode, ErrorResponse) = (toStatusCode(response.status), response)
