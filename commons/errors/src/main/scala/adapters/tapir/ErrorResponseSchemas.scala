package org.aulune.commons
package errors
package adapters.tapir


import errors.ErrorDetails
import errors.ErrorInfo
import errors.ErrorReason
import errors.ErrorResponse
import errors.ErrorStatus

import sttp.tapir.SchemaType.SInteger
import sttp.tapir.SchemaType.SString
import sttp.tapir.Schema
import sttp.tapir.Validator


/** Tapir schemas for [[ErrorResponse]]. */
object ErrorResponseSchemas:
  given Schema[ErrorResponse] = Schema.derived

  private given Schema[ErrorStatus] = Schema(
    schemaType = SInteger(),
  )
  private given Schema[ErrorDetails] = Schema.derived
  private given Schema[ErrorInfo] = Schema.derived
  private given Schema[ErrorReason] = Schema(
    schemaType = SString(),
  )
