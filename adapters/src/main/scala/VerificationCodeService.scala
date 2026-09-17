package org.aulune.authentigo
package adapters

import domain.token.{TotpSecret, VerificationCode}


/** Generates secrets, and generates and verifies short-lived codes for a given
 *  secret.
 *  @tparam F effect type.
 */
trait VerificationCodeService[F[_]]:
  /** Generates a new random secret. */
  def generateSecret: F[TotpSecret]

  /** Generates a code for the given secret, valid for a limited time. */
  def generateCode(secret: TotpSecret): F[VerificationCode]

  /** Verifies a code against the given secret.
   *  @return `true` if `code` is currently valid.
   */
  def verifyCode(secret: TotpSecret, code: VerificationCode): F[Boolean]
