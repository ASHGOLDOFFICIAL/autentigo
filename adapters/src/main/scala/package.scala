package org.aulune.authentigo.adapters

import org.aulune.commons.errors.{ErrorDetails, ErrorResponse, ErrorStatus}

/** Error domain shared by all `authentigo` service error responses. */
private[adapters] val Domain = "org.aulune.authentigo"


/** An unexpected, uncaught error occurred. */
private[adapters] val Internal: ErrorResponse = ErrorResponse(
  status = ErrorStatus.Internal,
  message = "Internal error.",
  details = ErrorDetails(),
)


/** A missing, invalid, or expired token was given. */
private[adapters] val Unauthenticated: ErrorResponse = ErrorResponse(
  status = ErrorStatus.Unauthenticated,
  message = "Given token is missing, invalid, or expired.",
  details = ErrorDetails(),
)
