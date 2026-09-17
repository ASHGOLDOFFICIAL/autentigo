package org.aulune.authentigo
package adapters
package user


/** Content for the password-reset-code email. */
private[user] object PasswordResetEmail:
  val Subject = "Your password reset code"

  def body(code: String): String = s"Your password reset code is: $code."
