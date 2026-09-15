package org.aulune.commons
package errors


/** Describes the cause of the error with structured details.
 *  @param info optional error information.
 */
final case class ErrorDetails(
    info: Option[ErrorInfo] = None,
)
