package org.aulune.authentigo.application
package user


/** Request to set a new password using an emailed reset code.
 *  @param email account email.
 *  @param code reset code sent to `email`.
 *  @param newPassword plain-text password to set.
 */
final case class ConfirmPasswordResetRequest(
    email: String,
    code: String,
    newPassword: String,
)
