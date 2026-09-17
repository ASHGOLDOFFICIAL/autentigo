package org.aulune.authentigo.application
package user

/** Request to email a password reset code to the given address.
 *  @param email address to send the reset code to.
 */
final case class RequestPasswordResetRequest(email: String)
